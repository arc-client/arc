
package com.arc.module.modules.render

import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listenOnce
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object Fullbright : Module(
    name = "Fullbright",
    description = "Makes everything brighter",
    tag = ModuleTag.RENDER,
) {
    private val nightVision by setting("Night Vision", false, description = "Adds the night vision effect client-side")
        .onValueChange { _, to -> setNightVision(to) }

    private val instance = StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 1, false, false)

    init {
        listenOnce<TickEvent.Pre> {
            if (nightVision && !player.hasStatusEffect(StatusEffects.NIGHT_VISION)) setNightVision(true)

            // Destroy the listener
            true
        }
    }

    private fun SafeContext.setNightVision(value: Boolean) {
        if (value) player.addStatusEffect(instance)
        else player.removeStatusEffect(StatusEffects.NIGHT_VISION)
    }
}
