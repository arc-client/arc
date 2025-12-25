
package com.arc.event.events

import com.arc.context.SafeContext
import com.arc.event.Event
import com.arc.event.callback.Cancellable
import com.arc.event.callback.ICancellable
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.graphics.renderer.esp.ShapeBuilder
import com.arc.graphics.renderer.esp.Treed

fun Any.onStaticRender(block: SafeContext.(ShapeBuilder) -> Unit) =
    listen<RenderEvent.Upload> { block(ShapeBuilder(Treed.Static.faceBuilder, Treed.Static.edgeBuilder)) }
fun Any.onDynamicRender(block: SafeContext.(ShapeBuilder) -> Unit) =
    listen<RenderEvent.Upload> { block(ShapeBuilder(Treed.Dynamic.faceBuilder, Treed.Dynamic.edgeBuilder)) }

sealed class RenderEvent {
    object Upload : Event
    object Render : Event

    class UpdateTarget : ICancellable by Cancellable()
}
