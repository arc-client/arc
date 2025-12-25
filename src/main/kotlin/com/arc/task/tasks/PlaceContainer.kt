
package com.arc.task.tasks

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.construction.blueprint.Blueprint.Companion.toStructure
import com.arc.interaction.construction.blueprint.StaticBlueprint.Companion.toBlueprint
import com.arc.interaction.construction.simulation.BuildSimulator.simulate
import com.arc.interaction.construction.simulation.result.results.GenericResult
import com.arc.interaction.construction.simulation.result.results.InteractResult
import com.arc.interaction.construction.verify.TargetState
import com.arc.interaction.managers.ManagerUtils
import com.arc.task.Task
import com.arc.task.tasks.BuildTask.Companion.build
import com.arc.threading.runSafeAutomated
import com.arc.util.BlockUtils.blockPos
import com.arc.util.item.ItemUtils.shulkerBoxes
import com.arc.util.math.distSq
import net.minecraft.block.ChestBlock
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PlaceContainer @Ta5kBuilder constructor(
    val stack: ItemStack,
    automated: Automated
) : Task<BlockPos>(), Automated by automated {
    private val startStack: ItemStack = stack.copy()
    override val name: String get() = "Placing container ${startStack.name.string}"

    override fun SafeContext.onStart() {
        val results = runSafeAutomated {
            BlockPos.iterateOutwards(player.blockPos, 4, 3, 4)
                .map { it.blockPos }
                .asSequence()
                .filter { !ManagerUtils.isPosBlocked(it) }
                .flatMap {
                    it.toStructure(TargetState.Stack(startStack))
                        .simulate()
                }
        }

        val options = results.filterIsInstance<InteractResult.Interact>().filter {
            canBeOpened(startStack, it.pos, it.context.hitResult.side)
        } + results.filterIsInstance<GenericResult.WrongItemSelection>()

        val containerPosition = options.filter {
            // ToDo: Check based on if we can move the player close enough rather than y level once the custom pathfinder is merged
            it.pos.y == player.blockPos.y
        }.minByOrNull { it.pos distSq player.pos }?.pos ?: run {
            failure("Couldn't find a valid container placement position for ${startStack.name.string}")
            return@onStart
        }

        containerPosition
            .toStructure(TargetState.Stack(startStack))
            .toBlueprint()
            .build(finishOnDone = true, collectDrops = false)
            .finally { success(containerPosition) }
            .execute(this@PlaceContainer)
    }

    private fun SafeContext.canBeOpened(
        itemStack: ItemStack,
        blockPos: BlockPos,
        direction: Direction,
    ) = when (itemStack.item) {
        Items.ENDER_CHEST -> {
            !ChestBlock.isChestBlocked(world, blockPos)
        }
        in shulkerBoxes -> {
            val box = ShulkerEntity
                .calculateBoundingBox(0.5f, direction, 0.0f, blockPos.toBottomCenterPos())
                .offset(blockPos)
                .contract(1.0E-6)

            world.isSpaceEmpty(box)
        }
        else -> false
    }
}
