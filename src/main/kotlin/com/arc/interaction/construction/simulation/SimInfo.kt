
package com.arc.interaction.construction.simulation

import com.arc.context.Automated
import com.arc.context.AutomatedSafeContext
import com.arc.interaction.construction.simulation.processing.PreProcessingData
import com.arc.interaction.construction.simulation.processing.ProcessorRegistry.getProcessingInfo
import com.arc.interaction.construction.simulation.result.BuildResult
import com.arc.interaction.construction.simulation.checks.BasicChecker.hasBasicRequirements
import com.arc.interaction.construction.simulation.checks.BreakSim.Companion.simBreak
import com.arc.interaction.construction.simulation.checks.InteractSim.Companion.simInteraction
import com.arc.interaction.construction.verify.TargetState
import com.arc.util.BlockUtils.matches
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*

/**
 * An interface representing all the information required to simulate a state. All simulators must present their public api
 * as an extension of the [SimInfo] class to allow easy access through the DSL style sim builder.
 */
interface SimInfo : Automated {
    val pos: BlockPos
    val state: BlockState
	val targetState: TargetState
    val pov: Vec3d
	val dependencyStack: Stack<Sim<*>>
    val concurrentResults: MutableSet<BuildResult>

	@SimDsl
	context(_: AutomatedSafeContext, _: Sim<*>)
	suspend fun sim()

    companion object {
        /**
         * Creates a [SimInfo], checks its basic requirements, and runs the [simBuilder] block.
         */
        @SimDsl
        context(_: BuildSimulator)
        suspend fun AutomatedSafeContext.sim(
            pos: BlockPos,
            state: BlockState,
            targetState: TargetState,
            pov: Vec3d,
            concurrentResults: MutableSet<BuildResult>
        ) = getTypedInfo(
	        pos,
	        state,
	        targetState,
	        pov,
	        Stack(),
	        concurrentResults
		).takeIf { it.hasBasicRequirements() }?.sim()

        /**
         * Creates a new [SimInfo] using the current [SimInfo]'s [dependencyStack] and [concurrentResults],
         * checks its basic requirements, and runs the [simBuilder] block. As simulations tend to make use of
         * concurrency, a new stack is created and the dependencies from the previous stack are added.
         */
        @SimDsl
        context(automatedSafeContext: AutomatedSafeContext, _: Sim<*>)
        suspend fun SimInfo.sim(
            pos: BlockPos = this.pos,
            state: BlockState = this.state,
            targetState: TargetState = this.targetState,
            pov: Vec3d = this.pov
        ) = automatedSafeContext.getTypedInfo(
	        pos,
	        state,
	        targetState,
	        pov,
	        Stack<Sim<*>>().apply { addAll(dependencyStack) },
	        concurrentResults
		).takeIf { it.hasBasicRequirements() }?.sim()

	    @SimDsl
	    private fun AutomatedSafeContext.getTypedInfo(
		    pos: BlockPos,
		    state: BlockState,
		    targetState: TargetState,
		    pov: Vec3d,
		    dependencyStack: Stack<Sim<*>>,
		    concurrentResults: MutableSet<BuildResult>
		): SimInfo {
			if (!targetState.isEmpty()) {
				getProcessingInfo(state, targetState, pos)?.let { preProcessing ->
					return object : InteractSimInfo, Automated by this {
						override val pos = pos
						override val state = state
						override val targetState = targetState
						override val pov = pov
						override val dependencyStack = dependencyStack
						override val concurrentResults = concurrentResults
						override val preProcessing = preProcessing
						override val expectedState = preProcessing.info.expectedState
						override val item = preProcessing.info.item
						override val placing = preProcessing.info.placing

						context(_: AutomatedSafeContext, _: Sim<*>)
						override suspend fun sim() = simInteraction()

						override fun AutomatedSafeContext.matchesTarget(state: BlockState, completely: Boolean) =
							expectedState.matches(state, if (!completely) preProcessing.info.ignore else emptySet())
					}
				}
			}

		    return object : BreakSimInfo, Automated by this {
			    override val pos = pos
			    override val state = state
			    override val targetState = targetState
			    override val pov = pov
			    override val dependencyStack = dependencyStack
			    override val concurrentResults = concurrentResults

			    context(_: AutomatedSafeContext, _: Sim<*>)
			    override suspend fun sim() = simBreak()
		    }
		}
    }
}

interface InteractSimInfo : SimInfo {
	val preProcessing: PreProcessingData
	val expectedState: BlockState
	val item: Item?
	val placing: Boolean

	fun AutomatedSafeContext.matchesTarget(state: BlockState, completely: Boolean): Boolean
}

interface BreakSimInfo : SimInfo