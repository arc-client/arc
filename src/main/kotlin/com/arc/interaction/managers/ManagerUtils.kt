
package com.arc.interaction.managers

import com.arc.event.Event
import com.arc.util.reflections.getInstances
import net.minecraft.util.math.BlockPos

object ManagerUtils {
    val managers = getInstances<Manager<*>>()
    val accumulatedManagerPriority = managers.map { it.stagePriority }.reduce { acc, priority -> acc + priority }
    val positionBlockingManagers = getInstances<PositionBlocking>()

    fun DebugLogger.newTick() =
        system("------------- New Tick -------------")

    fun DebugLogger.newStage(tickStage: Event?) =
        system("Tick stage ${tickStage?.run { this.toLogContext() }}")

    fun Event.toLogContext() = this::class.qualifiedName?.substringAfter("com.arc.event.events.")

    fun isPosBlocked(pos: BlockPos) =
        positionBlockingManagers.any { pos in it.blockedPositions }
}