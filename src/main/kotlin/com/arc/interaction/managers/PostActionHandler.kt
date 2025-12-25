
package com.arc.interaction.managers

import com.arc.context.Automated
import com.arc.event.events.ConnectionEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.event.listener.UnsafeListener.Companion.listenUnsafe
import com.arc.interaction.managers.breaking.BrokenBlockHandler
import com.arc.util.collections.LimitedDecayQueue

/**
 * A simple interface for handlers of actions that need some sort of server response after being executed.
 */
abstract class PostActionHandler<T : ActionInfo> {
    abstract val pendingActions: LimitedDecayQueue<T>

    init {
        listen<TickEvent.Pre>(priority = Int.MAX_VALUE) {
            pendingActions.cleanUp()
        }

        listenUnsafe<ConnectionEvent.Connect.Pre>(priority = Int.MIN_VALUE) {
            pendingActions.clear()
        }
    }

    fun T.startPending() {
        pendingActions.add(this)
        pendingInteractionsList.add(context)
    }

    fun T.stopPending() {
        pendingActions.remove(this)
        pendingInteractionsList.remove(context)
    }

    fun Automated.setPendingConfigs() {
        BrokenBlockHandler.pendingActions.setSizeLimit(buildConfig.maxPendingActions)
        BrokenBlockHandler.pendingActions.setDecayTime(buildConfig.actionTimeout * 50L)
    }
}