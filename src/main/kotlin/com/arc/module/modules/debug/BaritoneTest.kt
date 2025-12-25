
package com.arc.module.modules.debug

import baritone.api.pathing.goals.GoalXZ
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.BaritoneManager
import com.arc.module.Module
import com.arc.module.tag.ModuleTag

object BaritoneTest : Module(
    name = "BaritoneTest",
    description = "Test Baritone",
    tag = ModuleTag.DEBUG,
) {
    init {
        listen<TickEvent.Pre> {
            BaritoneManager.setGoalAndPath(GoalXZ(0, 0))
        }

        onDisable {
            BaritoneManager.cancel()
        }
    }
}
