
package com.arc.module.modules.player

import com.arc.Arc.mc
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag

object FastPlace : Module(
    name = "FastPlace",
    description = "Removes the place delay.",
    tag = ModuleTag.PLAYER
) {
    private val delay by setting("Delay", 0, 0..4, 1, "The delay between placing blocks.")

    init {
        listen<TickEvent.Pre> {
            if (mc.itemUseCooldown > delay) {
                mc.itemUseCooldown = delay
            }
        }
    }
}
