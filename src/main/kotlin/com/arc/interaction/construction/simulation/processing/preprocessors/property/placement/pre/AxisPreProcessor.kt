
package com.arc.interaction.construction.simulation.processing.preprocessors.property.placement.pre

import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.PropertyPreProcessor
import net.minecraft.block.BlockState
import net.minecraft.state.property.Properties

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object AxisPreProcessor : PropertyPreProcessor {
    override fun acceptsState(targetState: BlockState) =
        Properties.AXIS in targetState

    override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState) {
        val axis = targetState.get(Properties.AXIS)
        retainSides { side -> side.axis == axis }
    }
}
