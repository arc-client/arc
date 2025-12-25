
package com.arc.interaction.construction.simulation.processing.preprocessors.state

import com.arc.context.SafeContext
import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.StateProcessor
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FlowerPotBlock
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object FlowerPotPreProcessor : StateProcessor {
	override fun acceptsState(state: BlockState, targetState: BlockState) =
		(state.isReplaceable || state.block == Blocks.FLOWER_POT) &&
				(targetState.block is FlowerPotBlock && targetState.block != Blocks.FLOWER_POT)

	context(safeContext: SafeContext)
	override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState, pos: BlockPos) {
		if (state.block != Blocks.FLOWER_POT) {
			setExpectedState(Blocks.FLOWER_POT.defaultState)
			setItem(Items.FLOWER_POT)
			return
		}
		setPlacing(false)
	}
}