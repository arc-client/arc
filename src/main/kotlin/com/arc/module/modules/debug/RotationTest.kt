
package com.arc.module.modules.debug

import com.arc.config.AutomationConfig
import com.arc.config.groups.RotationSettings
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.rotating.RotationRequest
import com.arc.interaction.managers.rotating.visibilty.lookAt
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import net.minecraft.util.hit.HitResult

object RotationTest : Module(
    name = "RotationTest",
    tag = ModuleTag.DEBUG,
) {
    override val rotationConfig = RotationSettings(this, AutomationConfig.Group.Rotation)
    var hitPos: HitResult? = null
    
    init {
        onEnable {
            hitPos = mc.crosshairTarget
        }

        listen<TickEvent.Pre> {
            hitPos?.let { RotationRequest(lookAt(it.pos), this@RotationTest).submit() }
        }
    }
}
