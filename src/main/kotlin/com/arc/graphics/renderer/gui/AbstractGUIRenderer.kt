
package com.arc.graphics.renderer.gui

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.graphics.RenderMain
import com.arc.graphics.buffer.vertex.attributes.VertexAttrib
import com.arc.graphics.buffer.vertex.attributes.VertexMode
import com.arc.graphics.pipeline.VertexPipeline
import com.arc.graphics.shader.Shader
import com.arc.gui.components.ClickGuiLayout
import com.arc.module.modules.render.StyleEditor
import com.arc.util.math.MathUtils.toInt
import com.arc.util.math.Vec2d
import org.lwjgl.glfw.GLFW

open class AbstractGUIRenderer(
    attribGroup: VertexAttrib.Group,
    val shader: Shader
) {
    private val pipeline = VertexPipeline(VertexMode.Triangles, attribGroup)
    private var memoryMapping = true

    init {
        listen<TickEvent.Render.Pre>(alwaysListen = true) {
            memoryMapping = StyleEditor.useMemoryMapping
        }

        listen<TickEvent.Render.Post>(alwaysListen = true) {
            if (memoryMapping) pipeline.sync()
        }
    }

    fun render(
        shade: Boolean = false,
        block: VertexPipeline.(Shader) -> Unit
    ) {
        shader.use()

        block(pipeline, shader)

        shader["u_Shade"] = shade.toInt().toDouble()
        if (shade) {
            shader["u_ShadeTime"] = GLFW.glfwGetTime() * ClickGuiLayout.colorSpeed * 5.0
            shader["u_ShadeColor1"] = ClickGuiLayout.primaryColor
            shader["u_ShadeColor2"] = ClickGuiLayout.secondaryColor

            shader["u_ShadeSize"] = RenderMain.screenSize / Vec2d(ClickGuiLayout.colorWidth, ClickGuiLayout.colorHeight)
        }

        pipeline.apply {
            render()
            if (memoryMapping) end() else clear()
        }
    }
}
