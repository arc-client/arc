
package com.arc.module.modules.combat

import com.arc.context.SafeContext
import com.arc.event.events.PlayerEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.rotating.Rotation
import com.arc.interaction.managers.rotating.Rotation.Companion.rotationTo
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.extension.rotation
import com.arc.util.math.component1
import com.arc.util.math.component2
import com.arc.util.math.component3
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object Criticals : Module(
    name = "Criticals",
    description = "Forces your hits to be critical",
    tag = ModuleTag.COMBAT,
) {
    private val mode by setting("Mode", Mode.Grim)

    enum class Mode {
        Grim
    }

    init {
        listen<PlayerEvent.Attack.Entity> {
            when (mode) {
                Mode.Grim -> {
                    if (player.isOnGround) posPacket(0.00000001, rotation = player.rotation)
                    posPacket(-0.000000001, rotation = player.eyePos.rotationTo(it.entity.boundingBox.center))

                    connection.sendPacket(PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, player.yaw, player.pitch)) // TODO: This is wrong, fix it
                    connection.sendPacket(
                        PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN,
                            Direction.DOWN
                        )
                    )
                }
            }
        }
    }

    private fun SafeContext.posPacket(yOffset: Double, ground: Boolean = false, rotation: Rotation?) {
        val (x, y, z) = player.pos
        val collidesHorizontally = player.horizontalCollision

        val packet = rotation?.let {
            PlayerMoveC2SPacket.Full(x, y + yOffset, z, it.yawF, it.pitchF, ground, collidesHorizontally)
        } ?: PlayerMoveC2SPacket.PositionAndOnGround(x, y + yOffset, z, ground, collidesHorizontally)

        connection.sendPacket(packet)
    }
}
