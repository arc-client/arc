
package com.arc.interaction.managers.breaking

import com.arc.context.Automated
import com.arc.context.AutomatedSafeContext
import com.arc.context.SafeContext
import com.arc.interaction.construction.simulation.BuildSimulator.simulate
import com.arc.interaction.construction.simulation.context.BreakContext
import com.arc.interaction.construction.simulation.context.BuildContext
import com.arc.interaction.construction.simulation.result.BuildResult
import com.arc.interaction.construction.simulation.result.Dependent
import com.arc.interaction.construction.simulation.result.results.BreakResult
import com.arc.interaction.construction.verify.TargetState
import com.arc.interaction.managers.LogContext
import com.arc.interaction.managers.LogContext.Companion.LogContextBuilder
import com.arc.interaction.managers.Request
import com.arc.interaction.managers.breaking.BreakRequest.Companion.breakRequest
import com.arc.threading.runSafe
import com.arc.util.BlockUtils.blockState
import com.arc.util.BlockUtils.isEmpty
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.BlockPos

/**
 * Contains the information necessary for initializing and continuing breaks within the [BreakManager].
 *
 * The callbacks can be used to keep track of the break progress.
 *
 * A private constructor is used to force use of the cleaner [BreakRequestDsl] builder. This is
 * accessed through the [breakRequest] method.
 *
 * @param contexts A collection of [BreakContext]'s gathered from the BuildSimulator.
 * @param pendingInteractions A mutable, concurrent list to store the pending actions.
 *
 * @see com.arc.interaction.construction.simulation.BuildSimulator
 */
data class BreakRequest private constructor(
    val contexts: Collection<BreakContext>,
    val pendingInteractions: MutableCollection<BuildContext>,
    private val automated: Automated,
    override val nowOrNothing: Boolean = false
) : Request(), LogContext, Automated by automated {
    override val requestId = ++requestCount
    override val tickStageMask get() = breakConfig.tickStageMask

    var onStart: (SafeContext.(BlockPos) -> Unit)? = null
    var onUpdate: (SafeContext.(BlockPos) -> Unit)? = null
    var onStop: (SafeContext.(BlockPos) -> Unit)? = null
    var onCancel: (SafeContext.(BlockPos) -> Unit)? = null
    var onItemDrop: (SafeContext.(ItemEntity) -> Unit)? = null
    var onReBreakStart: (SafeContext.(BlockPos) -> Unit)? = null
    var onReBreak: (SafeContext.(BlockPos) -> Unit)? = null

    override val done: Boolean
        get() = runSafe { contexts.all { blockState(it.blockPos).isEmpty } } == true

    override fun getLogContextBuilder(): LogContextBuilder.() -> Unit = {
        group("Break Request") {
            value("Request ID", requestId)
            value("Contexts", contexts.size)
            group("Callbacks") {
                value("onStart", onStart != null)
                value("onUpdate", onUpdate != null)
                value("onStop", onStop != null)
                value("onCancel", onCancel != null)
                value("onItemDrop", onItemDrop != null)
                value("onReBreakStart", onReBreakStart != null)
                value("onReBreak", onReBreak != null)
            }
        }
    }

    @DslMarker
    annotation class BreakRequestDsl

	@BreakRequestDsl
	override fun submit(queueIfMismatchedStage: Boolean) =
		BreakManager.request(this, queueIfMismatchedStage)

    @BreakRequestDsl
    class BreakRequestBuilder(
        contexts: Collection<BreakContext>,
        pendingInteractions: MutableCollection<BuildContext>,
        nowOrNothing: Boolean,
        automated: Automated
    ) {
        val request = BreakRequest(contexts, pendingInteractions, automated, nowOrNothing)

        @BreakRequestDsl
        fun onStart(callback: SafeContext.(BlockPos) -> Unit) {
            request.onStart = callback
        }

        @BreakRequestDsl
        fun onUpdate(callback: SafeContext.(BlockPos) -> Unit) {
            request.onUpdate = callback
        }

        @BreakRequestDsl
        fun onStop(callback: SafeContext.(BlockPos) -> Unit) {
            request.onStop = callback
        }

        @BreakRequestDsl
        fun onCancel(callback: SafeContext.(BlockPos) -> Unit) {
            request.onCancel = callback
        }

        @BreakRequestDsl
        fun onItemDrop(callback: SafeContext.(ItemEntity) -> Unit) {
            request.onItemDrop = callback
        }

        @BreakRequestDsl
        fun onReBreakStart(callback: SafeContext.(BlockPos) -> Unit) {
            request.onReBreakStart = callback
        }

        @BreakRequestDsl
        fun onReBreak(callback: SafeContext.(BlockPos) -> Unit) {
            request.onReBreak = callback
        }
    }

    companion object {
        var requestCount = 0

	    @BreakRequestDsl
	    @JvmName("breakRequest1")
	    fun AutomatedSafeContext.breakRequest(
			positions: Collection<BlockPos>,
			pendingInteractions: MutableCollection<BuildContext>,
			nowOrNothing: Boolean = false,
			builder: (BreakRequestBuilder.() -> Unit)? = null
		) = positions
			.associateWith { TargetState.Empty }
		    .simulate()
		    .breakRequest(pendingInteractions, nowOrNothing, builder)

	    @BreakRequestDsl
	    @JvmName("breakRequest2")
	    context(automated: Automated)
	    fun Collection<BuildResult>.breakRequest(
			pendingInteractions: MutableCollection<BuildContext>,
			nowOrNothing: Boolean = false,
			builder: (BreakRequestBuilder.() -> Unit)? = null
		) = asSequence()
		    .map { if (it is Dependent) it.lastDependency else it }
		    .filterIsInstance<BreakResult.Break>()
		    .sorted()
			.map { it.context }
		    .toSet()
		    .takeIf { it.isNotEmpty() }
		    ?.let { automated.breakRequest(it, pendingInteractions, nowOrNothing, builder) }

        @BreakRequestDsl
        @JvmName("breakRequest3")
        fun Automated.breakRequest(
            contexts: Collection<BreakContext>,
            pendingInteractions: MutableCollection<BuildContext>,
            nowOrNothing: Boolean = false,
            builder: (BreakRequestBuilder.() -> Unit)? = null
        ) = BreakRequestBuilder(
	        contexts, pendingInteractions, nowOrNothing, this
		).apply { builder?.invoke(this) }.build()

        @BreakRequestDsl
        private fun BreakRequestBuilder.build(): BreakRequest = request
    }
}
