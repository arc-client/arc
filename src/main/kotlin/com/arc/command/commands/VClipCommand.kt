
package com.arc.command.commands

import com.arc.Arc.mc
import com.arc.brigadier.argument.double
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.util.extension.CommandBuilder

object VClipCommand : ArcCommand(
    name = "vclip",
    usage = "vclip <distance>",
    description = "Teleports the player up a specified distance"
) {
    override fun CommandBuilder.create() {
        required(double("distance")) { distance ->
            execute {
                val player = mc.player ?: return@execute
                val distance = distance().value()
                player.vehicle?.let { vehicle ->
                    vehicle.setPos(vehicle.x, vehicle.y + distance, vehicle.z)
                }
                player.setPos(player.x, player.y + distance, player.z)
            }
        }
    }
}