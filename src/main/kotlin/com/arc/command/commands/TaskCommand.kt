
package com.arc.command.commands

import com.arc.brigadier.argument.literal
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.task.RootTask
import com.arc.util.Communication.info
import com.arc.util.extension.CommandBuilder

object TaskCommand : ArcCommand(
    name = "task",
    usage = "task <cancel|clear>",
    description = "Control tasks"
) {
    override fun CommandBuilder.create() {
        required(literal("cancel")) {
            execute {
                this@TaskCommand.info("Cancelling all tasks")
                RootTask.cancel()
            }
        }

        required(literal("clear")) {
            execute {
                this@TaskCommand.info("Clearing all tasks")
                RootTask.cancel()
                RootTask.clear()
            }
        }
    }
}
