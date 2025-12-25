
package com.arc.module.modules.network

import com.arc.event.events.PacketEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.PlayerPacketHandler
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.Communication.warn
import com.arc.util.math.dist
import com.arc.util.math.distSq
import com.arc.util.text.buildText
import com.arc.util.text.color
import com.arc.util.text.literal
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import java.awt.Color

// ToDo: Should also include last packet info as HUD element and connection state.
//  We should find a better name.
//  Also should pause baritone on lag.
object Rubberband : Module(
    name = "Rubberband",
    description = "Info about rubberbands",
    tag = ModuleTag.NETWORK,
) {
    private val showLastPacketInfo by setting("Show Last Packet", true)
    private val showConnectionState by setting("Show Connection State", true)
    private val showRubberbandInfo by setting("Show Rubberband Info", true)

    init {
        listen<PacketEvent.Receive.Pre> { event ->
            if (!showRubberbandInfo) return@listen
            if (event.packet !is PlayerPositionLookS2CPacket) return@listen

            if (PlayerPacketHandler.configurations.isEmpty()) {
                this@Rubberband.warn("Position was reverted")
                return@listen
            }

            val newPos = event.packet.change.position
            val last = PlayerPacketHandler.configurations.minBy {
                it.position distSq newPos
            }

            this@Rubberband.warn(buildText {
                literal("Reverted position by ")
                color(Color.YELLOW) {
                    literal("${PlayerPacketHandler.configurations.toList().asReversed().indexOf(last) + 1}")
                }
                literal(" ticks (deviation: ")
                color(Color.YELLOW) {
                    literal("%.3f".format(last.position dist newPos))
                }
                literal(")")
            })
        }
    }
}
