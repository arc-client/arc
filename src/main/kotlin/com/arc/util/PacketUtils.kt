
package com.arc.util

import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.ClientConnection
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.listener.ServerPlayPacketListener
import net.minecraft.network.packet.Packet

object PacketUtils {
    /**
     * Sends a packet through the regular packet pipeline
     */
    fun ClientPlayNetworkHandler.sendPacket(block: () -> Packet<*>) = connection.send(block())

    /**
     * Sends a packet to the server without notifying the client.
     * It bypasses the mixins that would normally intercept the packet
     * and send it through the client's event bus.
     */
    fun ClientPlayNetworkHandler.sendPacketSilently(packet: Packet<*>) {
        if (!connection.isOpen || connection.packetListener?.accepts(packet) == true) return

        connection.send(packet, null, true)
        connection.packetsSentCounter++
    }

    /**
     * Handles a packet without notifying the client.
     * It bypasses the mixins that would normally intercept the packet
     * and send it through the client's event bus.
     */
    fun ClientPlayNetworkHandler.handlePacketSilently(packet: Packet<*>) {
        if (!connection.isOpen || connection.packetListener?.accepts(packet) == false) return

        ClientConnection.handlePacket(packet, connection.packetListener)
        connection.packetsReceivedCounter++
    }
}

typealias ClientPacket = Packet<out ClientPlayPacketListener>
typealias ServerPacket = Packet<out ServerPlayPacketListener>
