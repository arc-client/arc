
package com.arc.module.hud

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafe
import com.arc.util.SpeedUnit
import net.minecraft.util.math.Vec3d

object Speed : HudModule(
    name = "Speed",
    description = "Displays player speed",
    tag = ModuleTag.HUD
) {
    var speedUnit by setting("Speed Unit", SpeedUnit.MetersPerSecond)
    var onlyHorizontal by setting("Horizontal Speed", false, description = "Only consider horizontal movement for speed calculation")

    var previousPos: Vec3d = Vec3d.ZERO
    var currentPos: Vec3d = Vec3d.ZERO
    var speed: Double = 0.0

    init {
        listen<TickEvent.Post> {
            previousPos = currentPos
            currentPos = player.pos

            var vecDelta = player.pos.subtract(previousPos)
            if (onlyHorizontal) {
                vecDelta = Vec3d(vecDelta.x, 0.0, vecDelta.z)
            }
            speed = speedUnit.convertFromMinecraft(vecDelta.length())
        }

        onEnable {
            previousPos = player.pos
            currentPos = player.pos
            speed = 0.0
        }

        onDisable { speed = 0.0 }
    }

    override fun ImGuiBuilder.buildLayout() {
        runSafe {
            text("Speed: %.2f %s".format(speed, speedUnit.unitName))
        }
    }
}