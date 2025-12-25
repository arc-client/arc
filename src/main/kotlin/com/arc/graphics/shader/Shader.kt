
package com.arc.graphics.shader

import com.arc.Arc.mc
import com.arc.graphics.RenderMain
import com.arc.graphics.shader.ShaderUtils.createShaderProgram
import com.arc.graphics.shader.ShaderUtils.loadShader
import com.arc.graphics.shader.ShaderUtils.uniformMatrix
import com.arc.util.ArcResource
import com.arc.util.math.Vec2d
import com.arc.util.text
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20C.glGetUniformLocation
import org.lwjgl.opengl.GL20C.glUniform1f
import org.lwjgl.opengl.GL20C.glUniform1i
import org.lwjgl.opengl.GL20C.glUniform2f
import org.lwjgl.opengl.GL20C.glUniform3f
import org.lwjgl.opengl.GL20C.glUniform4f
import org.lwjgl.opengl.GL20C.glUseProgram
import java.awt.Color

class Shader(vertex: ArcResource, fragment: ArcResource) {
    private val uniformCache: Object2IntMap<String> = Object2IntOpenHashMap()

    private val id: Int = createShaderProgram(
        loadShader(ShaderType.VertexShader, vertex.text),
        loadShader(ShaderType.FragmentShader, fragment.text)
    )

	fun use() {
        glUseProgram(id)
        set("u_ProjModel", RenderMain.projModel)

        val x = mc.gameRenderer.camera.pos.x.toFloat()
        val y = mc.gameRenderer.camera.pos.y.toFloat()
        val z = mc.gameRenderer.camera.pos.z.toFloat()

        val view = Matrix4f()
            .translation(-x, -y, -z)

        set("u_View", view)
    }

    private fun loc(name: String) =
        if (uniformCache.containsKey(name))
            uniformCache.getInt(name)
        else
            glGetUniformLocation(id, name).let { location ->
                uniformCache.put(name, location)
                location
            }

    operator fun set(name: String, v: Boolean) =
        glUniform1i(loc(name), if (v) 1 else 0)

    operator fun set(name: String, v: Int) =
        glUniform1i(loc(name), v)

    operator fun set(name: String, v: Double) =
        glUniform1f(loc(name), v.toFloat())

    operator fun set(name: String, vec: Vec2d) =
        glUniform2f(loc(name), vec.x.toFloat(), vec.y.toFloat())

    operator fun set(name: String, vec: Vec3d) =
        glUniform3f(loc(name), vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())

    operator fun set(name: String, color: Color) =
        glUniform4f(
            loc(name),
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )

    operator fun set(name: String, mat: Matrix4f) =
        uniformMatrix(loc(name), mat)
}
