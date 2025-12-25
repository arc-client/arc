
package com.arc.module.modules.combat

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.interaction.construction.blueprint.TickingBlueprint.Companion.tickingBlueprint
import com.arc.interaction.construction.verify.TargetState
import com.arc.interaction.managers.interacting.InteractConfig
import com.arc.module.Module
import com.arc.module.modules.combat.PlayerTrap.getTrapPositions
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.task.Task
import com.arc.task.tasks.BuildTask.Companion.build
import com.arc.util.item.ItemUtils.block
import com.arc.util.player.SlotUtils.hotbarAndStorage
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem

object Surround : Module(
	name = "Surround",
	description = "Surrounds your players feet with any given block",
	tag = ModuleTag.COMBAT
) {
	private val blocks by setting("Blocks", setOf(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.CRYING_OBSIDIAN))

	private var task: Task<*>? = null

	init {
		setDefaultAutomationConfig {
			applyEdits {
				buildConfig.apply {
					editTyped(
						::pathing,
						::stayInRange,
						::spleefEntities,
						::collectDrops
					) { defaultValue(false); hide() }
					::checkSideVisibility.edit { defaultValue(false) }
				}
				interactConfig.apply {
					::airPlace.edit { defaultValue(InteractConfig.AirPlaceMode.Grim) }
				}
				hideGroup(eatConfig)
			}
		}

		onEnable {
			task = tickingBlueprint {
				val block = player.hotbarAndStorage.firstOrNull {
					it.item is BlockItem && blocks.contains(it.item.block)
				}?.item?.block ?: return@tickingBlueprint emptyMap()
				getTrapPositions(player)
					.filter { it.y <= player.blockPos.y }
					.associateWith { TargetState.Block(block) }
			}.build(finishOnDone = false).run()
		}
		onDisable { task?.cancel(); task = null }
	}
}