
package com.arc.module.modules.debug

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.Communication.info
import com.arc.util.combat.DamageUtils.fallDamage
import com.arc.util.combat.DamageUtils.isFallDeadly

object FallTest : Module(
    name = "FallTest",
    tag = ModuleTag.DEBUG,
) {
    init {
        listen<TickEvent.Pre> {
            val damage = fallDamage()

            info("Fall damage = $damage, Deadly = ${isFallDeadly()}")
        }
    }
}
