
package com.arc.interaction.construction.simulation.context

import com.arc.context.Automated
import com.arc.graphics.renderer.esp.ShapeBuilder
import com.arc.interaction.managers.LogContext
import com.arc.interaction.managers.LogContext.Companion.LogContextBuilder
import com.arc.interaction.managers.LogContext.Companion.getLogContextBuilder
import com.arc.interaction.managers.Request.Companion.submit
import com.arc.interaction.managers.hotbar.HotbarRequest
import com.arc.interaction.managers.interacting.InteractRequest
import com.arc.interaction.managers.rotating.RotationRequest
import net.minecraft.block.BlockState
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color

data class InteractContext(
    override val hitResult: BlockHitResult,
    override val rotationRequest: RotationRequest,
    override var hotbarIndex: Int,
    override val blockPos: BlockPos,
    override var cachedState: BlockState,
    override val expectedState: BlockState,
    val placing: Boolean,
    val sneak: Boolean,
    val currentDirIsValid: Boolean = false,
    private val automated: Automated
) : BuildContext(), LogContext, Automated by automated {
    private val baseColor = Color(35, 188, 254, 50)
    private val sideColor = Color(35, 188, 254, 100)

    override val sorter get() = interactConfig.sorter

    override fun ShapeBuilder.buildRenderer() {
        val box = with(hitResult.pos) {
            Box(
                x - 0.05, y - 0.05, z - 0.05,
                x + 0.05, y + 0.05, z + 0.05,
            ).offset(hitResult.side.doubleVector.multiply(0.05))
        }
        box(box, baseColor, sideColor)
    }

    fun requestDependencies(request: InteractRequest): Boolean {
        val hotbarRequest = submit(HotbarRequest(hotbarIndex, this), false)
        val validRotation = if (request.interactConfig.rotate) {
            submit(rotationRequest, false).done && currentDirIsValid
        } else true
        return hotbarRequest.done && validRotation
    }

    override fun getLogContextBuilder(): LogContextBuilder.() -> Unit = {
        group("Place Context") {
            text(blockPos.getLogContextBuilder())
            text(hitResult.getLogContextBuilder())
            text(rotationRequest.getLogContextBuilder())
            value("Hotbar Index", hotbarIndex)
            value("Cached State", cachedState)
            value("Expected State", expectedState)
            value("Sneak", sneak)
            value("Current Dir Is Valid", currentDirIsValid)
        }
    }
}
