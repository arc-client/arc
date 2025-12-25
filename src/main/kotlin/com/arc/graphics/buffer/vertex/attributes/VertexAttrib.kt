
package com.arc.graphics.buffer.vertex.attributes

import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL20C.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20C.glVertexAttribPointer
import org.lwjgl.opengl.GL33.glVertexAttribDivisor

sealed class VertexAttrib(
    private val componentCount: Int,
    componentSize: Int,
    private val normalized: Boolean,
    private val single: Boolean,
    private val type: Int
) {
    open class Float(
        normalized: Boolean = false, single: Boolean = false
    ) : VertexAttrib(1, 4, normalized, single, GL_FLOAT) {
        companion object : Float()
    }

    open class Vec2(
        normalized: Boolean = false, single: Boolean = false
    ) : VertexAttrib(2, 4, normalized, single, GL_FLOAT) {
        companion object : Vec2()
    }

    open class Vec3(
        normalized: Boolean = false, single: Boolean = false
    ) : VertexAttrib(3, 4, normalized, single, GL_FLOAT) {
        companion object : Vec3()
    }

    open class Color(
        normalized: Boolean = true, single: Boolean = false
    ) : VertexAttrib(4, 1, normalized, single, GL_UNSIGNED_BYTE) {
        companion object : Color()
    }

    val size = componentCount * componentSize

    fun link(index: Int, pointer: Long, stride: Int) {
        glEnableVertexAttribArray(index)
        glVertexAttribPointer(index, componentCount, type, normalized, stride, pointer)
        if (single) glVertexAttribDivisor(index, 1)
    }

    @Suppress("ClassName")
    open class Group(vararg val attributes: VertexAttrib) {
        // GUI
        object FONT : Group(
            Vec3, Vec2, Color
        )

        // WORLD
        object DYNAMIC_RENDERER : Group(
            Vec3, Vec3, Color
        )

        object STATIC_RENDERER : Group(
            Vec3, Color
        )

        object PARTICLE : Group(
            Vec3, Vec2, Color
        )

        val stride = attributes.sumOf { it.size }

        fun link() {
            attributes.foldIndexed(0L) { index, pointer, attrib ->
                attrib.link(index, pointer, stride)
                pointer + attrib.size
            }
        }
    }
}
