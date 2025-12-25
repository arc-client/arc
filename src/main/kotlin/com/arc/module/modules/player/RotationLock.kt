
package com.arc.module.modules.player

import com.arc.config.applyEdits
import com.arc.config.groups.RotationSettings
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.rotating.Rotation
import com.arc.interaction.managers.rotating.RotationMode
import com.arc.interaction.managers.rotating.RotationRequest
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.NamedEnum
import kotlin.math.roundToInt

object RotationLock : Module(
    name = "RotationLock",
    description = "Locks the player rotation to the given configuration",
    tag = ModuleTag.PLAYER,
) {
    private enum class Group(override val displayName: String) : NamedEnum {
        General("General"),
        Rotation("Rotation")
    }

    @JvmStatic val yawMode by setting("Yaw Mode", Mode.Snap).group(Group.General)
    private val yawStep by setting("Yaw Step", 45.0, 1.0..180.0, 1.0) { yawMode == Mode.Snap }.group(Group.General)
    private val customYaw by setting("Custom Yaw", 0.0, -179.0..180.0, 1.0) { yawMode == Mode.Custom }.group(Group.General)
    @JvmStatic val pitchMode by setting("Pitch Mode", Mode.None).group(Group.General)
    private val pitchStep by setting("Pitch Step", 45.0, 1.0..90.0, 1.0) { pitchMode == Mode.Snap }.group(Group.General)
    private val customPitch by setting("Custom Pitch", 0.0, -90.0..90.0, 1.0) { pitchMode == Mode.Custom }.group(Group.General)

    override val rotationConfig = RotationSettings(this, Group.Rotation).apply {
        applyEdits {
            ::rotationMode.edit { defaultValue(RotationMode.Lock) }
        }
    }

    init {
        listen<TickEvent.Pre> {
            val yaw = when (yawMode) {
                Mode.Custom -> customYaw
                Mode.Snap -> {
                    val normalizedYaw = (player.yaw % 360.0 + 360.0) % 360.0
                    (normalizedYaw / yawStep).roundToInt() * yawStep
                }
                Mode.None -> player.yaw.toDouble()
            }
            val pitch = when (pitchMode) {
                Mode.Custom -> customPitch
                Mode.Snap -> {
                    val clampedPitch = player.pitch.coerceIn(-90f, 90f)
                    (clampedPitch / pitchStep).roundToInt() * pitchStep
                }
                Mode.None -> player.pitch.toDouble()
            }

            RotationRequest(Rotation(yaw, pitch), this@RotationLock).submit()
        }
    }

    enum class Mode {
        Snap,
        Custom,
        None
    }
}
