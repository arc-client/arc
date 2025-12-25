
package com.arc.command.commands

import com.arc.brigadier.argument.literal
import com.arc.brigadier.argument.string
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.network.CapeManager
import com.arc.network.CapeManager.updateCape
import com.arc.util.Communication.info
import com.arc.util.Communication.logError
import com.arc.util.extension.CommandBuilder

object CapeCommand : ArcCommand(
    name = "cape",
    usage = "set <id>",
    description = "Sets your cape",
) {
    override fun CommandBuilder.create() {
        required(literal("set")) {
            required(string("id")) { id ->
                suggests { _, builder ->
                    CapeManager.availableCapes
                        .forEach { builder.suggest(it) }

                    builder.buildFuture()
                }

                execute {
                    val cape = id().value()
                    updateCape(cape) { error ->
                        if (error != null) logError("Could not update your cape", error)
                        else info("Updated your cape to $cape")
                    }
                }
            }
        }
    }
}
