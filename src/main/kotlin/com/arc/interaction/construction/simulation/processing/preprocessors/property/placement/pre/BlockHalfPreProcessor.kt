
package com.arc.interaction.construction.simulation.processing.preprocessors.property.placement.pre

import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.PropertyPreProcessor
import com.arc.interaction.construction.verify.ScanMode
import com.arc.interaction.construction.verify.SurfaceScan
import net.minecraft.block.BlockState
import net.minecraft.block.enums.BlockHalf
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object BlockHalfPreProcessor : PropertyPreProcessor {
    override fun acceptsState(targetState: BlockState) =
        Properties.BLOCK_HALF in targetState

    override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState) {
        val slab = targetState.get(Properties.BLOCK_HALF) ?: return

        val surfaceScan = when (slab) {
            BlockHalf.BOTTOM -> SurfaceScan(ScanMode.LesserBlockHalf, Direction.Axis.Y)
            BlockHalf.TOP -> SurfaceScan(ScanMode.GreaterBlockHalf, Direction.Axis.Y)
        }

        offerSurfaceScan(surfaceScan)
    }
}
