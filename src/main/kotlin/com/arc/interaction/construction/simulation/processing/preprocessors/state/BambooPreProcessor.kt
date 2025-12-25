
package com.arc.interaction.construction.simulation.processing.preprocessors.state

import com.arc.context.SafeContext
import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.StateProcessor
import com.arc.util.BlockUtils.blockState
import com.arc.util.BlockUtils.item
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object BambooPreProcessor : StateProcessor {
	override fun acceptsState(state: BlockState, targetState: BlockState) =
		(state.isReplaceable || state.block == Blocks.BAMBOO_SAPLING) && targetState.block == Blocks.BAMBOO

	context(safeContext: SafeContext)
	override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState, pos: BlockPos) {
		if (state.block == Blocks.BAMBOO_SAPLING) {
			omitPlacement()
			return
		}
		noCaching()
		if (safeContext.blockState(pos.down()).block.item != Items.BAMBOO) {
			setExpectedState(Blocks.BAMBOO_SAPLING.defaultState)
		}
	}
}