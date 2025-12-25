
package com.arc.module.modules.player

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.event.events.PlayerEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.construction.simulation.context.BuildContext
import com.arc.interaction.managers.breaking.BreakRequest.Companion.breakRequest
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafeAutomated
import java.util.concurrent.ConcurrentLinkedQueue

object FastBreak : Module(
    name = "FastBreak",
    description = "Break blocks faster.",
    tag = ModuleTag.PLAYER,
) {
    private val pendingActions = ConcurrentLinkedQueue<BuildContext>()

    init {
		setDefaultAutomationConfig {
			applyEdits {
				hideAllGroupsExcept(buildConfig, breakConfig, rotationConfig, hotbarConfig)
				buildConfig.apply {
					hide(
						::pathing,
						::stayInRange,
						::spleefEntities,
						::maxBuildDependencies,
						::collectDrops,
						::blockReach,
						::entityReach
					)
					::maxBuildDependencies.edit { defaultValue(0) }
					editTyped(
						::checkSideVisibility,
						::strictRayCast
					) { defaultValue(false); hide() }
					::blockReach.edit { defaultValue(Double.MAX_VALUE) }
				}
				breakConfig.apply {
					editTyped(
						::avoidLiquids,
						::avoidSupporting,
						::efficientOnly,
						::suitableToolsOnly
					) { defaultValue(false) }
					editTyped(
						::rotate,
						::doubleBreak
					) { defaultValue(false); hide() }
					::breaksPerTick.edit { defaultValue(1); hide() }
					::tickStageMask.edit { defaultValue(mutableSetOf(TickEvent.Input.Post)); hide() }
					hide(::sorter, ::unsafeCancels)
				}
				hotbarConfig::tickStageMask.edit { defaultValue(mutableSetOf(TickEvent.Input.Post)); hide() }
			}
		}

        listen<PlayerEvent.Attack.Block> { it.cancel() }
        listen<PlayerEvent.Breaking.Update> { event ->
            event.cancel()
	        runSafeAutomated {
				breakRequest(listOf(event.pos), pendingActions)?.submit()
			}
        }
    }
}
