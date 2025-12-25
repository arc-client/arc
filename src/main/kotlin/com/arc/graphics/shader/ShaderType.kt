
package com.arc.graphics.shader

import com.arc.graphics.gl.GLObject
import org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER

enum class ShaderType(override val gl: Int) : GLObject {
    FragmentShader(GL_FRAGMENT_SHADER),
    VertexShader(GL_VERTEX_SHADER)
}
