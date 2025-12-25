
package com.arc.command.commands

import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.literal
import com.arc.brigadier.argument.string
import com.arc.brigadier.argument.value
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.config.Configuration
import com.arc.util.Communication.info
import com.arc.util.extension.CommandBuilder

object ConfigCommand : ArcCommand(
    name = "config",
    aliases = setOf("cfg", "settings", "setting"),
    usage = "config <save | load | set> <configurable> <setting> <value>",
    description = "Save or load configuration files, or set any settings value",
    examples = listOf("config save", "config load", "config set Killaura Range 4.5")
) {
    override fun CommandBuilder.create() {
        required(literal("save")) {
            executeWithResult {
                Configuration.configurations.forEach { config ->
                    config.trySave(true)
                }
                this@ConfigCommand.info("Saved ${Configuration.configurations.size} configuration files.")
                return@executeWithResult success()
            }
        }
        required(literal("load")) {
            executeWithResult {
                Configuration.configurations.forEach { config ->
                    config.tryLoad()
                }
                this@ConfigCommand.info("Loaded ${Configuration.configurations.size} configuration files.")
                return@executeWithResult success()
            }
        }
        required(literal("reset")) {
            required(string("config")) { config ->
                suggests { _, builder ->
                    Configuration.configurables.forEach {
                        builder.suggest(it.commandName)
                    }
                    builder.buildFuture()
                }
                required(string("setting")) { setting ->
                    suggests { ctx, builder ->
                        val conf = config(ctx).value()
                        Configuration.configurableByName(conf)?.let { configurable ->
                            configurable.settings.forEach {
                                builder.suggest(it.commandName)
                            }
                        }
                        builder.buildFuture()
                    }
                    executeWithResult {
                        val confName = config().value()
                        val settingName = setting().value()
                        val configurable = Configuration.configurableByCommandName(confName) ?: run {
                            return@executeWithResult failure("$confName is not a valid configurable.")
                        }
                        val setting = Configuration.settingByCommandName(configurable, settingName) ?: run {
                            return@executeWithResult failure("$settingName is not a valid setting for $confName.")
                        }
                        setting.reset()
                        return@executeWithResult success()
                    }
                }
            }
        }
        required(literal("set")) {
            Configuration.configurables.forEach { configurable ->
                required(literal(configurable.commandName)) {
                    configurable.settings.forEach { setting ->
                        required(literal(setting.commandName)) {
                            with(setting) {
                                buildCommand(registry)
                            }
                        }
                    }
                }
            }
        }
    }
}
