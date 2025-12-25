
package com.arc.interaction.construction.simulation.context

import com.arc.context.Automated
import com.arc.graphics.renderer.esp.ShapeBuilder
import com.arc.interaction.material.StackSelection
import com.arc.interaction.managers.LogContext
import com.arc.interaction.managers.LogContext.Companion.LogContextBuilder
import com.arc.interaction.managers.LogContext.Companion.getLogContextBuilder
import com.arc.interaction.managers.rotating.RotationRequest
import com.arc.threading.runSafe
import com.arc.util.BlockUtils.emptyState
import net.minecraft.block.BlockState
import net.minecraft.block.FallingBlock
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color
import kotlin.math.sqrt

data class BreakContext(
    override val hitResult: BlockHitResult,
    override val rotationRequest: RotationRequest,
    override val hotbarIndex: Int,
    val itemSelection: StackSelection,
    val instantBreak: Boolean,
    val insideBlock: Boolean,
    override var cachedState: BlockState,
    private val automated: Automated
) : BuildContext(), LogContext, Automated by automated {
    private val baseColor = Color(222, 0, 0, 25)
    private val sideColor = Color(222, 0, 0, 100)

    override val blockPos: BlockPos = hitResult.blockPos
    override val expectedState = cachedState.emptyState
    override val sortDistance = runSafe {
        val pov = player.eyePos
        val vec = hitResult.pos
        val d = vec.x - pov.x
        val e = (vec.y - pov.y).let {
            if (cachedState.block is FallingBlock) it - (buildConfig.entityReach / 2)
            else it
        }
        val f = vec.z - pov.z
        sqrt(d * d + e * e + f * f)
    } ?: Double.MAX_VALUE

    override val sorter get() = breakConfig.sorter

    override fun ShapeBuilder.buildRenderer() {
        val box = with(hitResult.pos) {
            Box(
                x - 0.05, y - 0.05, z - 0.05,
                x + 0.05, y + 0.05, z + 0.05,
            ).offset(hitResult.side.doubleVector.multiply(0.05))
        }
        box(box, baseColor, sideColor)
    }

    override fun getLogContextBuilder(): LogContextBuilder.() -> Unit = {
        group("Break Context") {
            text(blockPos.getLogContextBuilder())
            text(hitResult.getLogContextBuilder())
            text(rotationRequest.getLogContextBuilder())
            value("Hotbar Index", hotbarIndex)
            value("Instant Break", instantBreak)
            value("Cached State", cachedState)
            value("Expected State", expectedState)
        }
    }
}
