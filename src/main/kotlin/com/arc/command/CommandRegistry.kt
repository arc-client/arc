
package com.arc.command

import com.arc.command.CommandManager.dispatcher
import com.arc.config.Configurable
import com.arc.config.configurations.ArcConfig
import com.arc.core.Loadable
import com.arc.util.reflections.getInstances
import com.mojang.brigadier.tree.CommandNode

/**
 * The [CommandRegistry] object is responsible for managing all [ArcCommand] instances in the system.
 */
object CommandRegistry : Configurable(ArcConfig), Loadable {
    override val priority get() = -2
    override val name = "command"
    val prefix by setting("prefix", ';')

    val commands = getInstances<ArcCommand>().toMutableList()

    override fun load() = "Loaded ${commands.size} commands with ${dispatcher.root.children()} possible command paths."

    private fun CommandNode<*>.children(): Int = children.sumOf { it.children() } + 1
}
