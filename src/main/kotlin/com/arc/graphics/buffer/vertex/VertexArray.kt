
package com.arc.graphics.buffer.vertex

import com.arc.graphics.buffer.vertex.attributes.VertexAttrib
import com.arc.graphics.buffer.vertex.attributes.VertexMode
import com.arc.graphics.pipeline.PersistentBuffer
import org.lwjgl.opengl.GL30C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL30C.glBindVertexArray
import org.lwjgl.opengl.GL30C.glGenVertexArrays
import org.lwjgl.opengl.GL32C.glDrawElementsBaseVertex

class VertexArray(
    private val vertexMode: VertexMode,
    private val attributes: VertexAttrib.Group
) {
    private val vao = glGenVertexArrays()
    private var linkedVBO: PersistentBuffer? = null

    fun renderIndices(
        ibo: PersistentBuffer
    ) = linkedVBO?.let { vbo ->
        renderInternal(
            indicesSize = ibo.byteBuffer.bytesPut - ibo.uploadOffset,
            indicesPointer = ibo.byteBuffer.pointer + ibo.uploadOffset,
            verticesOffset = vbo.uploadOffset
        )
    } ?: throw IllegalStateException("Unable to use vertex array without having a VBO linked to it.")

    private fun renderInternal(
        indicesSize: Long,
        indicesPointer: Long,
        verticesOffset: Long
    ) {
        glBindVertexArray(vao)
        glDrawElementsBaseVertex(
            vertexMode.mode,
            indicesSize.toInt() / Int.SIZE_BYTES,
            GL_UNSIGNED_INT,
            indicesPointer,
            verticesOffset.toInt() / attributes.stride,
        )
        glBindVertexArray(0)
    }

    fun linkVbo(vbo: PersistentBuffer) {
        linkedVBO = vbo

        glBindVertexArray(vao)
        vbo.buffer.bind { attributes.link() }
        glBindVertexArray(0)
    }
}
