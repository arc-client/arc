
package com.arc.module.modules.debug

import com.arc.event.events.ClientEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.event.listener.SafeListener.Companion.listenConcurrently
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.Communication.info

object TimerTest : Module(
    name = "TimerTest",
    tag = ModuleTag.DEBUG,
) {
    private var last = 0L

    init {
        listen<ClientEvent.FixedTick> {
            val now = System.currentTimeMillis()
            info("${now - last} - Fixed Tick on game thread")
            last = now
        }

        listenConcurrently<ClientEvent.FixedTick> {
//            info("${System.currentTimeMillis()} - Fixed Tick Concurrently (but not on mc game thread)")
        }
    }
}
