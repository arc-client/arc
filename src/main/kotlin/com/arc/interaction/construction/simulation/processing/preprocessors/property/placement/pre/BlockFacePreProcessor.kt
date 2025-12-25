
package com.arc.interaction.construction.simulation.processing.preprocessors.property.placement.pre

import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.PropertyPreProcessor
import net.minecraft.block.BlockState
import net.minecraft.block.enums.BlockFace
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object BlockFacePreProcessor : PropertyPreProcessor {
    override fun acceptsState(targetState: BlockState) =
        Properties.BLOCK_FACE in targetState

    override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState) {
        val property = targetState.get(Properties.BLOCK_FACE) ?: return
	    when (property) {
			BlockFace.FLOOR -> retainSides(Direction.DOWN)
		    BlockFace.CEILING -> retainSides(Direction.UP)
		    BlockFace.WALL -> retainSides { it in Direction.Type.HORIZONTAL }
		}
    }
}
