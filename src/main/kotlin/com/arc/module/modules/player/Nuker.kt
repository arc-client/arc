
package com.arc.module.modules.player

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.interaction.BaritoneManager
import com.arc.interaction.construction.blueprint.TickingBlueprint.Companion.tickingBlueprint
import com.arc.interaction.construction.verify.TargetState
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.task.Task
import com.arc.task.tasks.BuildTask.Companion.build
import com.arc.util.BlockUtils.blockPos
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos

object Nuker : Module(
    name = "Nuker",
    description = "Breaks blocks around you",
    tag = ModuleTag.PLAYER,
) {
    private val height by setting("Height", 6, 1..8, 1)
    private val width by setting("Width", 6, 1..8, 1)
    private val flatten by setting("Flatten", true)
    private val onGround by setting("On Ground", false, "Only break blocks when the player is standing on ground")
    private val fillFluids by setting("Fill Fluids", false, "Removes liquids by filling them in before breaking")
    private val fillFloor by setting("Fill Floor", false)
    private val baritoneSelection by setting("Baritone Selection", false, "Restricts nuker to your baritone selection")

    private var task: Task<*>? = null

    init {
		setDefaultAutomationConfig {
            applyEdits {
                inventoryConfig::immediateAccessOnly.edit { defaultValue(true) }
            }
        }

        onEnable {
            task = tickingBlueprint {
                if (onGround && !player.isOnGround) return@tickingBlueprint emptyMap()

                val selection = BlockPos.iterateOutwards(player.blockPos, width, height, width)
                    .asSequence()
                    .map { it.blockPos }
                    .filter { !world.isAir(it) }
                    .filter { !flatten || it.y >= player.blockPos.y }
                    .filter { pos ->
                        if (!baritoneSelection) true
                        else BaritoneManager.primary.selectionManager.selections.any {
                            val min = it.min()
                            val max = it.max()
                            pos.x >= min.x && pos.x <= max.x
                                    && pos.y >= min.y && pos.y <= max.y
                                    && pos.z >= min.z && pos.z <= max.z
                        }
                    }
                    .associateWith { if (fillFluids) TargetState.Air else TargetState.Empty }

                if (fillFloor) {
                    val floor = BlockPos.iterateOutwards(player.blockPos.down(), width, 0, width)
                        .map { it.blockPos }
                        .associateWith { TargetState.Solid(setOf(Blocks.MAGMA_BLOCK)) }
                    return@tickingBlueprint selection + floor
                }

                selection
            }.build(finishOnDone = false)
				.run()
        }

        onDisable {
            task?.cancel()
        }
    }
}
