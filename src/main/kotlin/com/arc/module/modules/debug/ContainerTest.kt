
package com.arc.module.modules.debug

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.material.StackSelection.Companion.select
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.task.tasks.AcquireMaterial.Companion.acquire
import net.minecraft.item.Items

object ContainerTest : Module(
    name = "ContainerTest",
    description = "Test container",
    tag = ModuleTag.DEBUG,
) {
    init {
        listen<TickEvent.Pre> {
//            info(task.info)
        }

        onEnable {
            acquire {
                Items.OBSIDIAN.select()
            }.run()
        }
    }
}
