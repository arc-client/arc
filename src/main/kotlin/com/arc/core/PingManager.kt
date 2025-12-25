
package com.arc.core

import com.arc.event.events.PacketEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.util.collections.LimitedOrderedSet
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket
import net.minecraft.util.Util

object PingManager : Loadable {
    private val pings: LimitedOrderedSet<Long> = LimitedOrderedSet(100)
    private const val INTERVAL = 1

    override fun load() = "Loaded Ping Manager"

    val lastPing: Long
        get() = pings.lastOrNull() ?: 0

    init {
        listen<TickEvent.Pre> {
            connection.sendPacket(QueryPingC2SPacket(Util.getMeasuringTimeMs()))
        }

        listen<PacketEvent.Receive.Pre> { event ->
            if (event.packet !is PingResultS2CPacket) return@listen

            pings.add(Util.getMeasuringTimeMs() - event.packet.startTime)
        }
    }
}
