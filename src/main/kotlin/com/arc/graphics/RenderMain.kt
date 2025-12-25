
package com.arc.graphics

import com.arc.Arc.mc
import com.arc.event.EventFlow.post
import com.arc.event.events.RenderEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.graphics.gl.GlStateUtils.setupGL
import com.arc.graphics.gl.Matrices
import com.arc.graphics.gl.Matrices.resetMatrices
import com.arc.graphics.renderer.esp.Treed
import com.arc.util.math.Vec2d
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture
import org.joml.Matrix4f
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER

object RenderMain {
    val projectionMatrix = Matrix4f()
    val modelViewMatrix get() = Matrices.peek()
    val projModel: Matrix4f get() = Matrix4f(projectionMatrix).mul(modelViewMatrix)

    var screenSize = Vec2d.ZERO

    @JvmStatic
    fun render3D(positionMatrix: Matrix4f, projMatrix: Matrix4f) {
        resetMatrices(positionMatrix)
        projectionMatrix.set(projMatrix)

        setupGL {
            val framebuffer = mc.framebuffer
            val prevFramebuffer = (framebuffer.getColorAttachment() as GlTexture).getOrCreateFramebuffer(
                (RenderSystem.getDevice() as GlBackend).framebufferManager,
                null
            )

            GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, prevFramebuffer)

            Treed.Static.render()
            Treed.Dynamic.render()

            RenderEvent.Render.post()
        }
    }

    init {
        listen<TickEvent.Post> {
            Treed.Static.clear()
            Treed.Dynamic.clear()

            RenderEvent.Upload.post()

            Treed.Static.upload()
            Treed.Dynamic.upload()
        }
    }
}
