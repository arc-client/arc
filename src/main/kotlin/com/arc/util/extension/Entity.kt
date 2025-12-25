
package com.arc.util.extension

import com.arc.interaction.managers.rotating.Rotation
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Vec3d

val Entity.prevPos
    get() = Vec3d(lastX, lastY, lastZ)

val Entity.rotation
    get() = Rotation(yaw, pitch)

val LivingEntity.fullHealth: Double
    get() = health + absorptionAmount.toDouble()

var LivingEntity.isElytraFlying
    get() = isGliding
    set(value) {
        setFlag(7, value)
    }
