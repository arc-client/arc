
package com.arc.command.commands

import com.arc.Arc.mc
import com.arc.brigadier.argument.double
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.util.extension.CommandBuilder
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

object HClipCommand : ArcCommand(
    name = "hclip",
    usage = "hclip <distance>",
    description = "Teleports the player forward a specified distance"
) {
    override fun CommandBuilder.create() {
        required(double("distance")) { distance ->
            execute {
                val player = mc.player ?: return@execute
                val dir = Vec3d.fromPolar(Vec2f(player.pitch, player.yaw)).normalize()
                val distance = distance().value()
                val xBlocks = dir.x * distance
                val zBlocks = dir.z * distance
                player.vehicle?.let { vehicle ->
                    vehicle.setPos(vehicle.x + xBlocks, vehicle.y, vehicle.z + zBlocks)
                }
                player.setPos(player.x + xBlocks, player.y, player.z + zBlocks)
            }
        }
    }
}