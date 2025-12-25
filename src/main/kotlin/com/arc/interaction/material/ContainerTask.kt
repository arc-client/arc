
package com.arc.interaction.material

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.task.Task

abstract class ContainerTask : Task<Unit>() {
    private var finish = false
    private val delay = 5
    private var currentDelay = 0

    fun delayedFinish() {
        finish = true
    }

    init {
        listen<TickEvent.Post> {
            if (finish) {
                if (currentDelay++ > delay) success()
            }
        }
    }
}
