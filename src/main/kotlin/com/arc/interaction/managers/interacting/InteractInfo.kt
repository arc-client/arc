
package com.arc.interaction.managers.interacting

import com.arc.context.SafeContext
import com.arc.interaction.construction.simulation.context.BuildContext
import com.arc.interaction.construction.simulation.context.InteractContext
import com.arc.interaction.managers.ActionInfo
import com.arc.interaction.managers.LogContext
import com.arc.interaction.managers.LogContext.Companion.LogContextBuilder
import net.minecraft.util.math.BlockPos

data class InteractInfo(
	override val context: InteractContext,
	override val pendingInteractionsList: MutableCollection<BuildContext>,
	val onPlace: (SafeContext.(BlockPos) -> Unit)?,
	val interactConfig: InteractConfig
) : ActionInfo, LogContext {
    override fun getLogContextBuilder(): LogContextBuilder.() -> Unit = {
        group("Place Info") {
            text(context.getLogContextBuilder())
            group("Callbacks") {
                value("onPlace", onPlace != null)
            }
        }
    }
}