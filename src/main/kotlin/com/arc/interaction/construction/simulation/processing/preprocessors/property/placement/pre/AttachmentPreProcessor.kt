
package com.arc.interaction.construction.simulation.processing.preprocessors.property.placement.pre

import com.arc.interaction.construction.simulation.processing.PreProcessingInfoAccumulator
import com.arc.interaction.construction.simulation.processing.PropertyPreProcessor
import net.minecraft.block.BlockState
import net.minecraft.block.enums.Attachment
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

// Collected using reflections and then accessed from a collection in ProcessorRegistry
@Suppress("unused")
object AttachmentPreProcessor : PropertyPreProcessor {
    override fun acceptsState(targetState: BlockState) =
        Properties.ATTACHMENT in targetState

    override fun PreProcessingInfoAccumulator.preProcess(state: BlockState, targetState: BlockState) {
        val attachment = targetState.get(Properties.ATTACHMENT) ?: return
	    when (attachment) {
			Attachment.FLOOR -> retainSides(Direction.DOWN)
		    Attachment.CEILING -> retainSides(Direction.UP)
		    else -> retainSides { it in Direction.Type.HORIZONTAL }
		}
    }
}