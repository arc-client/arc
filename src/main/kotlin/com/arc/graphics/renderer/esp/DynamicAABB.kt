
package com.arc.graphics.renderer.esp

import com.arc.util.extension.prevPos
import com.arc.util.math.minus
import net.minecraft.entity.Entity
import net.minecraft.util.math.Box

class DynamicAABB {
    private var prev: Box? = null
    private var curr: Box? = null

    val pair get() = prev?.let { prev -> curr?.let { curr -> prev to curr } }

    fun update(box: Box): DynamicAABB {
        prev = curr ?: box
        curr = box

        return this
    }

    fun reset() {
        prev = null
        curr = null
    }

    companion object {
        val Entity.dynamicBox
            get() = DynamicAABB().apply {
                update(boundingBox.offset(prevPos - pos))
                update(boundingBox)
            }
    }
}
