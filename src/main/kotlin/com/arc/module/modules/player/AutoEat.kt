
package com.arc.module.modules.player

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.config.groups.EatConfig.Companion.reasonEating
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.task.tasks.EatTask
import com.arc.task.tasks.EatTask.Companion.eat
import com.arc.threading.runSafeAutomated

object AutoEat : Module(
    name = "AutoEat",
    description = "Eats food when you are hungry",
    tag = ModuleTag.PLAYER,
) {
    private var eatTask: EatTask? = null

    init {
		setDefaultAutomationConfig {
			applyEdits {
				hideAllGroupsExcept(eatConfig)
			}
		}

        listen<TickEvent.Pre> {
            val reason = runSafeAutomated { reasonEating() }
            if (eatTask != null || !reason.shouldEat()) return@listen

            val task = eat()
            task.finally { eatTask = null }
            task.run()
            eatTask = task
        }

        onDisable {
            eatTask?.cancel()
            eatTask = null
        }
    }
}
