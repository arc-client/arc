
package com.arc.util

import com.arc.Arc
import com.arc.Arc.mc
import com.arc.command.CommandRegistry
import com.arc.event.EventFlow
import com.arc.module.ModuleRegistry
import com.arc.util.Formatting.format
import com.arc.util.extension.tickDelta
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult

object DebugInfoHud {
    @JvmStatic
    fun MutableList<String>.addDebugInfo() {
        add("")
        add("" + Formatting.UNDERLINE + "Arc ${Arc.VERSION}+${mc.versionType}")
        add("Modules: ${ModuleRegistry.modules.size} with ${ModuleRegistry.modules.sumOf { it.settings.size }} settings")
        add("Commands: ${CommandRegistry.commands.size}")
        add("Synchronous Listeners: ${EventFlow.syncListeners.size}")
        add("Concurrent Listeners: ${EventFlow.concurrentListeners.size}")

        when (val hit = mc.crosshairTarget) {
            is BlockHitResult -> {
                add("Crosshair Target: Block")
                add("  Vec3d: %.5f, %.5f, %.5f".format(hit.pos.x, hit.pos.y, hit.pos.z))
                add("  BlockPos: ${hit.blockPos.toShortString()}")
                add("  Side: ${hit.side}")
            }

            is EntityHitResult -> {
                add("Crosshair Target: Entity")
                add("  Vec3d: ${hit.pos}")
                add("  Entity: ${hit.entity}")
            }

            null -> add("Crosshair Target: None")
        }

        add("Eye Pos: ${mc.cameraEntity?.getCameraPosVec(mc.tickDelta)?.format()}")

        return
    }
}
