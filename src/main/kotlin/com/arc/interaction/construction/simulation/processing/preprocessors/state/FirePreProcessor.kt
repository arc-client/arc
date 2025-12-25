
package com.arc.interaction.construction.simulation.processing.preprocessors.state

import com.arc.context.SafeContext
import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.StateProcessor
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object FirePreProcessor : StateProcessor {
	override fun acceptsState(state: BlockState, targetState: BlockState) =
		state.isReplaceable && targetState.block == Blocks.FIRE

	context(safeContext: SafeContext)
	override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState, pos: BlockPos) {
		setItem(Items.FLINT_AND_STEEL)
		setPlacing(false)
	}
}