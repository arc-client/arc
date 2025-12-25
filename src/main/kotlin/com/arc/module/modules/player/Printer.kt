
package com.arc.module.modules.player

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.interaction.construction.blueprint.TickingBlueprint
import com.arc.interaction.construction.verify.TargetState
import com.arc.interaction.managers.interacting.InteractConfig
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.task.Task
import com.arc.task.tasks.BuildTask.Companion.build
import com.arc.util.BlockUtils.blockPos
import com.arc.util.Communication.logError
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.world.SchematicWorldHandler
import net.minecraft.util.math.BlockPos

object Printer : Module(
    name = "Printer",
    description = "Automatically prints schematics",
    tag = ModuleTag.PLAYER
) {
    private fun isLitematicaAvailable(): Boolean = runCatching {
        Class.forName("fi.dy.masa.litematica.Litematica")
        true
    }.getOrDefault(false)

    private val range by setting("Range", 5, 1..7, 1)
    private val air by setting("Air", false)

    private var buildTask: Task<*>? = null

    init {
		setDefaultAutomationConfig {
			applyEdits {
				editTyped(buildConfig::pathing, buildConfig::stayInRange) { defaultValue(false) }
				editTyped(breakConfig::efficientOnly, breakConfig::suitableToolsOnly) { defaultValue(false) }
				interactConfig::airPlace.edit { defaultValue(InteractConfig.AirPlaceMode.Grim) }
                inventoryConfig::immediateAccessOnly.edit { defaultValue(true) }
			}
		}

        onEnable {
            if (!isLitematicaAvailable()) {
                logError("Litematica is not installed!")
                disable()
                return@onEnable
            }
            buildTask = TickingBlueprint {
                val schematicWorld = SchematicWorldHandler.getSchematicWorld() ?: return@TickingBlueprint emptyMap()
                BlockPos.iterateOutwards(player.blockPos, range, range, range)
                    .map { it.blockPos }
                    .asSequence()
                    .filter { DataManager.getRenderLayerRange().isPositionWithinRange(it) }
                    .associateWith { TargetState.State(schematicWorld.getBlockState(it)) }
                    .filter { air || !it.value.blockState.isAir }
            }.build(finishOnDone = false).run()
        }

        onDisable { buildTask?.cancel(); buildTask = null }
    }
}