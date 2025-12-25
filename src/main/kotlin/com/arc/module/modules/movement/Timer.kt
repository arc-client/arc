
package com.arc.module.modules.movement

import com.arc.event.events.ClientEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag

object Timer : Module(
    name = "Timer",
    description = "Modify client tick speed.",
    tag = ModuleTag.MOVEMENT,
) {
    private val timer by setting("Timer", 1.0, 0.0..10.0, 0.01)

    init {
        listen<ClientEvent.TimerUpdate> {
            it.speed = timer.coerceAtLeast(0.05)
        }
    }
}
