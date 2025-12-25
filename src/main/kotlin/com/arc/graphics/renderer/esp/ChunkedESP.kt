
package com.arc.graphics.renderer.esp

import com.arc.event.events.RenderEvent
import com.arc.event.events.TickEvent
import com.arc.event.events.WorldEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.event.listener.SafeListener.Companion.listenConcurrently
import com.arc.module.Module
import com.arc.module.modules.render.StyleEditor
import com.arc.threading.awaitMainThread
import com.arc.util.world.FastVector
import com.arc.util.world.fastVectorOf
import net.minecraft.world.World
import net.minecraft.world.chunk.WorldChunk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class ChunkedESP private constructor(
    owner: Module,
    private val update: ShapeBuilder.(World, FastVector) -> Unit
) {
    private val rendererMap = ConcurrentHashMap<Long, EspChunk>()
    private val WorldChunk.renderer
        get() = rendererMap.getOrPut(pos.toLong()) { EspChunk(this, this@ChunkedESP) }

    private val uploadQueue = ConcurrentLinkedDeque<() -> Unit>()
    private val rebuildQueue = ConcurrentLinkedDeque<EspChunk>()

    private var ticks = 0

    fun rebuild() {
        rebuildQueue.clear()
        rebuildQueue.addAll(rendererMap.values)
    }

    init {
        listen<WorldEvent.BlockUpdate.Client> { event ->
            world.getWorldChunk(event.pos).renderer.notifyChunks()
        }

        listen<WorldEvent.ChunkEvent.Load> { event ->
            event.chunk.renderer.notifyChunks()
        }

        listen<WorldEvent.ChunkEvent.Unload> {
            val pos = it.chunk.pos.toLong()
            rendererMap.remove(pos)?.notifyChunks()
        }

        owner.listenConcurrently<TickEvent.Pre> {
            val polls = minOf(StyleEditor.rebuildsPerTick, rebuildQueue.size)
            repeat(polls) { rebuildQueue.poll()?.rebuild() }
        }

        owner.listen<TickEvent.Pre>  {
            val polls = minOf(StyleEditor.uploadsPerTick, uploadQueue.size)
            repeat(polls) { uploadQueue.poll()?.invoke() }
        }

        owner.listen<RenderEvent.Render> {
            rendererMap.values.forEach { it.renderer.render() }
        }
    }

    companion object {
        fun Module.newChunkedESP(
            update: ShapeBuilder.(World, FastVector) -> Unit
        ) = ChunkedESP(this@newChunkedESP, update)
    }

    private class EspChunk(val chunk: WorldChunk, val owner: ChunkedESP) {
        var renderer = Treed(static = true)
        private val builder: ShapeBuilder
            get() = ShapeBuilder(renderer.faceBuilder, renderer.edgeBuilder)

        /*val neighbors = listOf(1 to 0, 0 to 1, -1 to 0, 0 to -1)
            .map { ChunkPos(chunk.pos.x + it.first, chunk.pos.z + it.second) }*/

        fun notifyChunks() {
            owner.rendererMap[chunk.pos.toLong()]?.let {
                if (!owner.rebuildQueue.contains(it))
                    owner.rebuildQueue.add(it)
            }
        }

        suspend fun rebuild() {
            renderer.clear()
            renderer = awaitMainThread { Treed(static = true) }

            for (x in chunk.pos.startX..chunk.pos.endX)
                for (z in chunk.pos.startZ..chunk.pos.endZ)
                    for (y in chunk.bottomY..chunk.height)
                        owner.update(builder, chunk.world, fastVectorOf(x, y, z))

            owner.uploadQueue.add { renderer.upload() }
        }
    }
}
