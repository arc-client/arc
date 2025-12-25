
package com.arc.interaction.construction.simulation.processing.preprocessors.property.placement.post

import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.ProcessorRegistry.standardInteractProperties
import com.arc.interaction.construction.simulation.processing.PropertyPostProcessor
import net.minecraft.block.BlockState

object StandardInteractPreProcessor : PropertyPostProcessor {
	override fun acceptsState(state: BlockState, targetState: BlockState) =
		standardInteractProperties.any {
			it in targetState && state.get(it) != targetState.get(it)
		}

	override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState) {
		setItem(null)
		setPlacing(false)
	}
}