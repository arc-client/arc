
package com.arc.module.modules.combat

import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.NamedEnum
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object AimAssist : Module(
    name = "AimAssist",
    description = "Smoothly rotates towards nearby entities",
    tag = ModuleTag.COMBAT,
) {
    // General
    private val range by setting("Range", 4.5, 1.0..6.0, 0.1, "Maximum range to target entities").group(Group.General)
    private val rotationSpeed by setting("Rotation Speed", 600.0, 10.0..3600.0, 10.0, "Rotation speed in degrees per second").group(Group.General)
    private val fov by setting("FOV", 120.0, 30.0..360.0, 10.0, "Field of view to detect targets").group(Group.General)

    // Targeting
    private val players by setting("Players", true, "Target players").group(Group.Targeting)
    private val hostiles by setting("Hostiles", false, "Target hostile mobs").group(Group.Targeting)
    private val passives by setting("Passives", false, "Target passive mobs").group(Group.Targeting)
    private val invisibles by setting("Invisibles", false, "Target invisible entities").group(Group.Targeting)

    private enum class Group(override val displayName: String) : NamedEnum {
        General("General"),
        Targeting("Targeting"),
    }

    init {
        listen<TickEvent.Pre> {
            val target = findTarget() ?: return@listen

            // Calculate rotation needed to face target
            val eyePos = player.eyePos
            val targetPos = target.eyePos
            val diff = targetPos.subtract(eyePos)

            val horizontalDist = sqrt(diff.x * diff.x + diff.z * diff.z)
            val neededYaw = Math.toDegrees(atan2(-diff.x, diff.z)).toFloat()
            val neededPitch = Math.toDegrees(-atan2(diff.y, horizontalDist)).toFloat().coerceIn(-90f, 90f)

            // Slowly turn towards target (like Wurst)
            val maxChange = (rotationSpeed / 20.0).toFloat() // degrees per tick

            val currentYaw = player.yaw
            val currentPitch = player.pitch

            // Calculate yaw difference (wrapped to -180..180)
            var yawDiff = wrapDegrees(neededYaw - currentYaw)
            var pitchDiff = neededPitch - currentPitch

            // Limit rotation speed
            if (abs(yawDiff) > maxChange) {
                yawDiff = if (yawDiff > 0) maxChange else -maxChange
            }
            if (abs(pitchDiff) > maxChange) {
                pitchDiff = if (pitchDiff > 0) maxChange else -maxChange
            }

            player.yaw = currentYaw + yawDiff
            player.pitch = (currentPitch + pitchDiff).coerceIn(-90f, 90f)
        }
    }

    private fun wrapDegrees(value: Float): Float {
        var wrapped = value % 360
        if (wrapped >= 180) wrapped -= 360
        if (wrapped < -180) wrapped += 360
        return wrapped
    }

    private fun SafeContext.findTarget(): LivingEntity? {
        return world.entities
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .filter { it.isAlive }
            .filter { player.distanceTo(it) <= range }
            .filter { isValidTarget(it) }
            .filter { isInFov(it) }
            .filter { invisibles || !it.isInvisible }
            .minByOrNull { getAngleToEntity(it) } // Prioritize closest to crosshair like Wurst
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        return when {
            entity is PlayerEntity && players -> true
            entity is Monster && hostiles -> true
            entity is PassiveEntity && passives -> true
            else -> false
        }
    }

    private fun SafeContext.isInFov(entity: Entity): Boolean {
        return getAngleToEntity(entity) <= fov / 2
    }

    private fun SafeContext.getAngleToEntity(entity: Entity): Double {
        val look = player.rotationVector
        val toEntity = entity.eyePos.subtract(player.eyePos).normalize()
        val dot = look.dotProduct(toEntity)
        return Math.toDegrees(kotlin.math.acos(dot.coerceIn(-1.0, 1.0)))
    }
}
