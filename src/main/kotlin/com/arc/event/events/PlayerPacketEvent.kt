
package com.arc.event.events

import com.arc.event.Event
import com.arc.event.callback.Cancellable
import com.arc.event.callback.ICancellable
import com.arc.interaction.managers.rotating.Rotation
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d

sealed class PlayerPacketEvent {
    data class Pre(
        var position: Vec3d,
        var rotation: Rotation,
        var onGround: Boolean,
        var isSprinting: Boolean,
        var isCollidingHorizontally: Boolean,
    ) : ICancellable by Cancellable()

    class Post : Event

    data class Send(
        val packet: PlayerMoveC2SPacket,
    ) : ICancellable by Cancellable()
}
