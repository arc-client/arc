
package com.arc.graphics.buffer.vertex.attributes

import org.lwjgl.opengl.GL11C.GL_LINES
import org.lwjgl.opengl.GL11C.GL_TRIANGLES

enum class VertexMode(val mode: Int) {
    Lines(GL_LINES),
    Triangles(GL_TRIANGLES)
}
