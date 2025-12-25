
package com.arc.command

import com.arc.brigadier.argument.literal
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.command.CommandManager.dispatcher
import com.arc.core.Loadable
import com.arc.util.Communication.info
import com.arc.util.Nameable
import com.arc.util.extension.CommandBuilder
import com.arc.util.text.ClickEvents
import com.arc.util.text.HoverEvents
import com.arc.util.text.buildText
import com.arc.util.text.clickEvent
import com.arc.util.text.highlighted
import com.arc.util.text.hoverEvent
import com.arc.util.text.literal
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.server.command.CommandManager

abstract class ArcCommand(
    final override val name: String,
    val aliases: Set<String> = emptySet(),
    val usage: String = "",
    val description: String = "",
    val examples: List<String> = listOf()
) : Nameable, Loadable {
    override val priority get() = -1

    val registry: CommandRegistryAccess by lazy {
        CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup())
    }

    override fun load(): String {
        (aliases + name).forEach { alias ->
            LiteralArgumentBuilder.literal<CommandSource>(alias.lowercase()).apply {
                create()
                help()
                dispatcher.register(this)
            }
        }
        return ""
    }

    private fun CommandBuilder.help() {
        required(literal("help")) {
            execute {
                this@ArcCommand.info(buildText {
                    literal("Help\n")
                    highlighted("Usage:\n")
                    literal("${CommandRegistry.prefix}$usage\n")
                    highlighted("Description:\n")
                    literal(description)
                    if (examples.isNotEmpty()) {
                        literal("\n")
                        highlighted("Examples:\n")
                        examples.forEachIndexed { i, example ->
                            val full = "${CommandRegistry.prefix}$example"
                            hoverEvent(HoverEvents.showText(buildText { literal("Click to try this example!") })) {
                                clickEvent(ClickEvents.suggestCommand(full)) {
                                    literal(full)
                                    if (i != examples.lastIndex) { literal("\n") }
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    abstract fun CommandBuilder.create()
}
