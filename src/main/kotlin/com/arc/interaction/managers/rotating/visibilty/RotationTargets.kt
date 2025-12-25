
package com.arc.interaction.managers.rotating.visibilty

import com.arc.context.AutomatedSafeContext
import com.arc.context.SafeContext
import com.arc.interaction.managers.rotating.Rotation
import com.arc.interaction.managers.rotating.RotationManager
import com.arc.interaction.managers.rotating.visibilty.VisibilityChecker.ALL_SIDES
import com.arc.interaction.managers.rotating.visibilty.VisibilityChecker.findRotation
import com.arc.util.extension.rotation
import net.minecraft.entity.Entity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.hypot

@DslMarker
annotation class RotationDsl

@RotationDsl
fun SafeContext.lookAt(pos: Vec3d): Rotation {
    val direction = pos.subtract(player.eyePos).normalize()
    val yaw = Math.toDegrees(atan2(direction.z, direction.x)) - 90.0
    val pitch = -Math.toDegrees(atan2(direction.y, hypot(direction.x, direction.z)))
    return Rotation(yaw, pitch)
}

@RotationDsl
fun SafeContext.lookInDirection(direction: PlaceDirection) =
    if (!direction.isInArea(player.rotation)) direction.snapToArea(RotationManager.activeRotation)
    else player.rotation

@RotationDsl
fun AutomatedSafeContext.lookAtHit(hit: HitResult) =
    when (hit) {
        is BlockHitResult -> lookAtBlock(hit.blockPos, setOf(hit.side))
        is EntityHitResult -> lookAtEntity(hit.entity)
        else -> null
    }

@RotationDsl
fun AutomatedSafeContext.lookAtEntity(entity: Entity, sides: Set<Direction> = ALL_SIDES) =
    entity.findRotation(buildConfig.entityReach, player.eyePos, sides)

@RotationDsl
fun AutomatedSafeContext.lookAtBlock(pos: BlockPos, sides: Set<Direction> = ALL_SIDES) =
    pos.findRotation(buildConfig.blockReach, player.eyePos, sides)