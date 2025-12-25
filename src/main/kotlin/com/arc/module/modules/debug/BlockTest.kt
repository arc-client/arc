
package com.arc.module.modules.debug

import com.arc.event.events.onStaticRender
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.world.blockSearch
import net.minecraft.block.Blocks
import net.minecraft.util.math.Vec3i
import java.awt.Color

object BlockTest : Module(
    name = "BlockTest",
    description = "BlockTest",
    tag = ModuleTag.DEBUG,
) {
    private val rangeX by setting("Range X", 5, 1..7, 1, "Range X")
    private val rangeY by setting("Range Y", 5, 1..7, 1, "Range Y")
    private val rangeZ by setting("Range Z", 5, 1..7, 1, "Range Z")
    private val stepX by setting("Step X", 1, 1..7, 1, "Step X")
    private val stepY by setting("Step Y", 1, 1..7, 1, "Step Y")
    private val stepZ by setting("Step Z", 1, 1..7, 1, "Step Z")

    private val range: Vec3i
        get() = Vec3i(rangeX, rangeY, rangeZ)

    private val step: Vec3i
        get() = Vec3i(stepX, stepY, stepZ)

    private val filledColor = Color(100, 150, 255, 128)
    private val outlineColor = Color(100, 150, 255, 51)

    init {
        onStaticRender {
            blockSearch(range, step = step) { _, state ->
                state.isOf(Blocks.DIAMOND_BLOCK)
            }.forEach { (pos, state) ->
                state.getOutlineShape(world, pos).boundingBoxes.forEach { box ->
                    it.box(box.offset(pos), filledColor, outlineColor)
                }
            }
        }
    }
}
