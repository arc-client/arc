
package com.arc.module.modules.render

import com.arc.Arc.mc
import com.arc.event.events.PlayerEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.rotating.Rotation
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.extension.rotation
import net.minecraft.client.option.Perspective


object FreeLook : Module(
    name = "FreeLook",
    description = "Allows you to look around freely while moving",
    tag = ModuleTag.PLAYER,
) {
    val enableYaw by setting("Enable Yaw", false, "Don't effect pitch if enabled")
    val enablePitch by setting("Enable Pitch", false, "Don't effect yaw if enabled")
    val togglePerspective by setting("Toggle Perspective", true, "Toggle perspective when enabling FreeLook")

    var camera: Rotation = Rotation.ZERO
    var previousPerspective: Perspective = Perspective.FIRST_PERSON

    /**
     * @see net.minecraft.entity.Entity.changeLookDirection
     */
    private const val SENSITIVITY_FACTOR = 0.15

    @JvmStatic
    fun updateCam() {
        mc.gameRenderer.apply {
            camera.setRotation(this@FreeLook.camera.yawF, this@FreeLook.camera.pitchF)
        }
    }

    init {
        previousPerspective = mc.options.perspective

        onEnable {
            camera = player.rotation
            previousPerspective = mc.options.perspective
            if (togglePerspective) mc.options.perspective = Perspective.THIRD_PERSON_BACK
        }

        onDisable {
            updateCam()
            mc.options.perspective = previousPerspective
        }

        listen<PlayerEvent.ChangeLookDirection> {
            if (!isEnabled) return@listen

            camera = camera.withDelta(
                it.deltaYaw * SENSITIVITY_FACTOR,
                it.deltaPitch * SENSITIVITY_FACTOR
            )

            if (enablePitch) player.pitch = camera.pitchF
            if (enableYaw) player.yaw = camera.yawF

            it.cancel()
        }
    }
}
