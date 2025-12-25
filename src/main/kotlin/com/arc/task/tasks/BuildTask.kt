
package com.arc.task.tasks

import baritone.api.pathing.goals.GoalBlock
import com.arc.Arc.LOG
import com.arc.config.AutomationConfig.Companion.DEFAULT
import com.arc.config.groups.EatConfig.Companion.reasonEating
import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.BaritoneManager
import com.arc.interaction.construction.blueprint.Blueprint
import com.arc.interaction.construction.blueprint.Blueprint.Companion.toStructure
import com.arc.interaction.construction.blueprint.PropagatingBlueprint
import com.arc.interaction.construction.blueprint.StaticBlueprint.Companion.toBlueprint
import com.arc.interaction.construction.blueprint.TickingBlueprint
import com.arc.interaction.construction.simulation.BuildGoal
import com.arc.interaction.construction.simulation.BuildSimulator.simulate
import com.arc.interaction.construction.simulation.Simulation.Companion.simulation
import com.arc.interaction.construction.simulation.context.BuildContext
import com.arc.interaction.construction.simulation.result.BuildResult
import com.arc.interaction.construction.simulation.result.Contextual
import com.arc.interaction.construction.simulation.result.Dependent
import com.arc.interaction.construction.simulation.result.Drawable
import com.arc.interaction.construction.simulation.result.Navigable
import com.arc.interaction.construction.simulation.result.Resolvable
import com.arc.interaction.construction.simulation.result.results.BreakResult
import com.arc.interaction.construction.simulation.result.results.GenericResult
import com.arc.interaction.construction.simulation.result.results.InteractResult
import com.arc.interaction.construction.simulation.result.results.PreSimResult
import com.arc.interaction.construction.verify.TargetState
import com.arc.interaction.managers.breaking.BreakRequest.Companion.breakRequest
import com.arc.interaction.managers.interacting.InteractRequest.Companion.interactRequest
import com.arc.interaction.managers.inventory.InventoryRequest.Companion.inventoryRequest
import com.arc.task.Task
import com.arc.task.tasks.EatTask.Companion.eat
import com.arc.threading.runSafeAutomated
import com.arc.util.Formatting.format
import com.arc.util.extension.Structure
import com.arc.util.extension.inventorySlots
import com.arc.util.item.ItemUtils.block
import com.arc.util.player.SlotUtils.hotbarAndStorage
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentLinkedQueue

class BuildTask private constructor(
    private val blueprint: Blueprint,
    private val finishOnDone: Boolean,
    private val collectDrops: Boolean,
    private val lifeMaintenance: Boolean,
    automated: Automated
) : Task<Structure>(), Automated by automated {
    override val name: String get() = "Building $blueprint with ${(breaks / (age / 20.0 + 0.001)).format(precision = 1)} b/s ${(placements / (age / 20.0 + 0.001)).format(precision = 1)} p/s"

    private val pendingInteractions = ConcurrentLinkedQueue<BuildContext>()
    private val atMaxPendingInteractions
        get() = pendingInteractions.size >= buildConfig.maxPendingActions

    private var placements = 0
    private var breaks = 0
    private val dropsToCollect = mutableSetOf<ItemEntity>()
    var eatTask: EatTask? = null

    private val onItemDrop: ((item: ItemEntity) -> Unit)?
        get() = if (collectDrops) { item ->
            dropsToCollect.add(item)
        } else null

    override fun SafeContext.onStart() {
        iteratePropagating()
    }

    init {
        listen<TickEvent.Pre> {
            when {
                lifeMaintenance && eatTask == null && runSafeAutomated { reasonEating() }.shouldEat() -> {
                    eatTask = eat()
                    eatTask?.finally {
                        eatTask = null
                    }?.execute(this@BuildTask)
                    return@listen
                }
                eatTask != null -> return@listen
            }

            if (blueprint is TickingBlueprint) {
                blueprint.tick() ?: failure("Failed to tick the ticking blueprint")
            }

            if (collectDrops()) return@listen

            simulateAndProcess()
        }

        listen<TickEvent.Post> {
            if (finishOnDone && blueprint.structure.isEmpty()) {
                failure("Structure is empty")
                return@listen
            }
        }
    }

    private fun SafeContext.simulateAndProcess() {
        val results = runSafeAutomated { blueprint.structure.simulate() }

        DEFAULT.drawables = results
            .filterIsInstance<Drawable>()
            .plus(pendingInteractions.toList())

        val resultsNotBlocked = results
            .filter { result -> pendingInteractions.none { it.blockPos == result.pos } }
            .sorted()

        val bestResult = resultsNotBlocked.firstOrNull() ?: return
        handleResult(bestResult, resultsNotBlocked)
    }

    private fun SafeContext.handleResult(result: BuildResult, allResults: List<BuildResult>) {
        if (result !is Dependent && result !is Contextual && pendingInteractions.isNotEmpty())
            return

        when (result) {
            is PreSimResult.Done,
            is PreSimResult.Unbreakable,
            is PreSimResult.Restricted,
            is PreSimResult.NoPermission,
            is GenericResult.Ignored -> {
                if (iteratePropagating()) {
                    simulateAndProcess()
                    return
                }

                if (finishOnDone) success(blueprint.structure)
            }

            is GenericResult.NotVisible,
            is InteractResult.NoIntegrity -> {
                if (!buildConfig.pathing) return
                val sim = blueprint.simulation()
                val goal = BuildGoal(sim, player.blockPos)
                BaritoneManager.setGoalAndPath(goal)
            }

            is Navigable -> {
                if (buildConfig.pathing) BaritoneManager.setGoalAndPath(result.goal)
            }

            is Contextual -> {
                if (atMaxPendingInteractions) return
                when (result) {
                    is BreakResult.Break ->
                        allResults.breakRequest(pendingInteractions) {
                            onStop { breaks++ }
                            onItemDrop?.let { onItemDrop ->
                                onItemDrop { onItemDrop(it) }
                            }
                        }?.submit()

                    is InteractResult.Interact -> {
	                    allResults.interactRequest(pendingInteractions, false) {
		                    onPlace { placements++ }
	                    }?.submit()
                    }
                }
            }

            is Dependent -> handleResult(result.lastDependency, allResults)

            is Resolvable -> {
	            LOG.info("Resolving: ${result.name}")
                result.resolve()?.execute(this@BuildTask)
            }
        }
    }

    private fun SafeContext.collectDrops() =
        dropsToCollect
            .firstOrNull()
            ?.let { itemDrop ->
                if (pendingInteractions.isNotEmpty()) return@let true

                if (!world.entities.contains(itemDrop)) {
                    dropsToCollect.remove(itemDrop)
                    BaritoneManager.cancel()
                    return@let true
                }

                if (player.hotbarAndStorage.none { it.isEmpty }) {
                    val stackToThrow = player.currentScreenHandler.inventorySlots.firstOrNull {
                        it.stack.item.block in inventoryConfig.disposables
                    } ?: run {
                        failure("No item in inventory to throw but inventory is full and cant pick up item drop")
                        return@let true
                    }
                    inventoryRequest {
                        throwStack(stackToThrow.id)
                    }.submit()
                    return@let true
                }

                BaritoneManager.setGoalAndPath(GoalBlock(itemDrop.blockPos))
                return@let true
            } ?: false

    fun iteratePropagating() =
        if (blueprint is PropagatingBlueprint) {
            blueprint.next() ?: failure("Failed to propagate the next blueprint")
            true
        } else false

    companion object {
        @Ta5kBuilder
        fun Automated.build(
            finishOnDone: Boolean = true,
            collectDrops: Boolean = buildConfig.collectDrops,
            lifeMaintenance: Boolean = false,
            blueprint: () -> Blueprint
        ) = BuildTask(blueprint(), finishOnDone, collectDrops, lifeMaintenance, this)

        @Ta5kBuilder
        context(automated: Automated)
        fun Structure.build(
            finishOnDone: Boolean = true,
            collectDrops: Boolean = automated.buildConfig.collectDrops,
            lifeMaintenance: Boolean = false
        ) = BuildTask(toBlueprint(), finishOnDone, collectDrops, lifeMaintenance, automated)

        @Ta5kBuilder
        context(automated: Automated)
        fun Blueprint.build(
            finishOnDone: Boolean = true,
            collectDrops: Boolean = automated.buildConfig.collectDrops,
            lifeMaintenance: Boolean = false
        ) = BuildTask(this, finishOnDone, collectDrops, lifeMaintenance, automated)

        @Ta5kBuilder
        fun Automated.breakAndCollectBlock(
            blockPos: BlockPos,
            finishOnDone: Boolean = true,
            collectDrops: Boolean = true,
            lifeMaintenance: Boolean = false
        ) = BuildTask(
            blockPos.toStructure(TargetState.Air).toBlueprint(),
            finishOnDone, collectDrops, lifeMaintenance, this
        )
    }
}
