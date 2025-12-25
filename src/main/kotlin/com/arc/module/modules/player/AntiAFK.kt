
package com.arc.module.modules.player

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import net.minecraft.util.Hand

object AntiAFK : Module(
    name = "AntiAFK",
    description = "Keeps you from getting kicked",
    tag = ModuleTag.PLAYER,
) {
    private val delay by setting("Delay", 300, 5..600, 1, unit = " s", description = "Delay between swinging the hand.")
    private val swingHand by setting("Swing Hand", Hand.MAIN_HAND, description = "Hand to swing.")

    init {
        listen<TickEvent.Pre> {
            if (mc.uptimeInTicks % (delay * 20) != 0L) return@listen
            player.swingHand(swingHand)
        }
    }
}
