
package com.arc.interaction.managers.interacting

import com.arc.config.AutomationConfig.Companion.DEFAULT
import com.arc.config.AutomationConfig.Companion.DEFAULT.managerDebugLogs
import com.arc.event.events.WorldEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.construction.simulation.processing.ProcessorRegistry
import com.arc.interaction.managers.PostActionHandler
import com.arc.interaction.managers.interacting.InteractManager.placeSound
import com.arc.threading.runSafe
import com.arc.util.BlockUtils.matches
import com.arc.util.Communication.warn
import com.arc.util.collections.LimitedDecayQueue

object InteractedBlockHandler : PostActionHandler<InteractInfo>() {
    override val pendingActions = LimitedDecayQueue<InteractInfo>(
        DEFAULT.buildConfig.maxPendingActions,
        DEFAULT.buildConfig.actionTimeout * 50L
    ) {
        if (managerDebugLogs) warn("${it::class.simpleName} at ${it.context.blockPos.toShortString()} timed out")
        if (it.interactConfig.interactConfirmationMode != InteractConfig.InteractConfirmationMode.AwaitThenPlace) {
            runSafe {
                world.setBlockState(it.context.blockPos, it.context.cachedState)
            }
        }
        it.pendingInteractionsList.remove(it.context)
    }

    init {
        listen<WorldEvent.BlockUpdate.Server>(priority = Int.MIN_VALUE) { event ->
            pendingActions
                .firstOrNull { it.context.blockPos == event.pos }
                ?.let { pending ->
                    if (!pending.context.expectedState.matches(event.newState)) {
                        if (pending.context.cachedState.matches(
                                event.newState,
                                ProcessorRegistry.postProcessedProperties
                            )
                        ) {
                            pending.context.cachedState = event.newState
                            return@listen
                        }

                        pending.stopPending()

                        if (managerDebugLogs) this@InteractedBlockHandler.warn("Placed block at ${event.pos.toShortString()} was rejected with ${event.newState} instead of ${pending.context.expectedState}")
                        return@listen
                    }

                    pending.stopPending()

                    if (pending.interactConfig.interactConfirmationMode == InteractConfig.InteractConfirmationMode.AwaitThenPlace)
                        with(pending.context) { placeSound(expectedState, blockPos) }
                    pending.onPlace?.invoke(this, pending.context.blockPos)
                }
        }
    }
}
