
package com.arc.graphics.renderer.esp

import com.arc.Arc.mc
import com.arc.graphics.buffer.vertex.attributes.VertexAttrib
import com.arc.graphics.buffer.vertex.attributes.VertexMode
import com.arc.graphics.gl.GlStateUtils
import com.arc.graphics.pipeline.VertexBuilder
import com.arc.graphics.pipeline.VertexPipeline
import com.arc.graphics.shader.Shader
import com.arc.module.modules.render.StyleEditor
import com.arc.util.extension.partialTicks

/**
 * Open class for 3d rendering. It contains two pipelines, one for edges and the other for faces.
 */
open class Treed(private val static: Boolean) {
    val shader = if (static) staticMode.first else dynamicMode.first

    val faces = VertexPipeline(VertexMode.Triangles, if (static) staticMode.second else dynamicMode.second)
    val edges = VertexPipeline(VertexMode.Lines, if (static) staticMode.second else dynamicMode.second)

    var faceBuilder = VertexBuilder(); private set
    var edgeBuilder = VertexBuilder(); private set

    fun upload() {
        faces.upload(faceBuilder)
        edges.upload(edgeBuilder)
    }

    fun render() {
        shader.use()

        if (!static)
            shader["u_TickDelta"] = mc.partialTicks

        GlStateUtils.withFaceCulling(faces::render)
        GlStateUtils.withLineWidth(StyleEditor.outlineWidth, edges::render)
    }

    fun clear() {
        faces.clear()
        edges.clear()

        faceBuilder = VertexBuilder()
        edgeBuilder = VertexBuilder()
    }

    /**
     * Public object for static rendering. Shapes rendering by this are not interpolated.
     * That means that if the shape is frequently moving, its movement will saccade.
     */
    object Static : Treed(true)

    /**
     * Public object for dynamic rendering. Its position will be interpolated between ticks, allowing
     * for smooth movement at the cost of duplicate position and slightly higher memory consumption.
     */
    object Dynamic : Treed(false)

    companion object {
        private val staticMode = Shader("shaders/vertex/box_static.glsl", "shaders/fragment/pos_color.glsl") to VertexAttrib.Group.STATIC_RENDERER
        private val dynamicMode = Shader("shaders/vertex/box_dynamic.glsl", "shaders/fragment/pos_color.glsl") to VertexAttrib.Group.DYNAMIC_RENDERER
    }
}
