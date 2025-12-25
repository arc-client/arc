
package com.arc.command.commands

import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.boolean
import com.arc.brigadier.argument.string
import com.arc.brigadier.argument.value
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.optional
import com.arc.brigadier.required
import com.arc.command.CommandRegistry.prefix
import com.arc.command.ArcCommand
import com.arc.module.ModuleRegistry
import com.arc.threading.runSafe
import com.arc.util.Communication.info
import com.arc.util.Communication.joinToText
import com.arc.util.Communication.warn
import com.arc.util.StringUtils.findSimilarStrings
import com.arc.util.extension.CommandBuilder
import com.arc.util.text.ClickEvents.suggestCommand
import com.arc.util.text.buildText
import com.arc.util.text.clickEvent
import com.arc.util.text.literal
import com.arc.util.text.styled
import java.awt.Color

object ModuleCommand : ArcCommand(
    name = "module",
    aliases = setOf("mod"),
    usage = "module <module name> [enable]",
    description = "Enable or disable a module"
) {
    override fun CommandBuilder.create() {
        executeWithResult {
            val enabled = ModuleRegistry.modules.filter {
                it.isEnabled
            }

            if (enabled.isEmpty()) {
                info("No modules are enabled")
                return@executeWithResult success()
            }

            this@ModuleCommand.info(buildText {
                styled(Color.GRAY) {
                    literal("Enabled Modules: ")
                }
                joinToText(enabled) {
                    clickEvent(suggestCommand("$prefix${input} ${it.name}")) {
                        styled(if (it.isEnabled) Color.GREEN else Color.RED) {
                            literal(it.name)
                        }
                    }
                }
            })
            return@executeWithResult success()
        }

        required(string("module name")) { moduleName ->
            suggests { _, builder ->
                ModuleRegistry.moduleNameMap.keys.forEach {
                    builder.suggest(it)
                }
                builder.buildFuture()
            }
            optional(boolean("enable")) { enable ->
                executeWithResult {
                    val name = moduleName().value()
                    val module = ModuleRegistry.modules.find {
                        it.name.equals(name, true)
                    } ?: return@executeWithResult failure(buildText {
                        styled(Color.RED) {
                            literal("Module ")
                            styled(Color.GRAY) {
                                literal("$name ")
                            }
                            literal("not found!")
                        }
                        val similarModules = name.findSimilarStrings(
                            ModuleRegistry.moduleNameMap.keys,
                            3
                        )
                        if (similarModules.isEmpty()) return@buildText

                        literal(" Did you mean ")
                        similarModules.forEachIndexed { index, s ->
                            if (index != 0) {
                                literal(", ")
                            }
                            clickEvent(suggestCommand("$prefix${input.replace(name, s)}")) {
                                styled(Color.GRAY) {
                                    literal(s)
                                }
                            }
                        }
                        literal("?")
                    })

                    runSafe {
                        if (enable == null) {
                            module.toggle()
                        } else {
                            if (enable().value() == module.isEnabled) {
                                this@ModuleCommand.warn(buildText {
                                    styled(Color.GRAY) {
                                        literal("$name already ")
                                        literal(if (module.isEnabled) "enabled" else "disabled")
                                    }
                                })
                                return@runSafe success()
                            }

                            if (enable().value()) {
                                module.enable()
                            } else {
                                module.disable()
                            }
                        }
                        this@ModuleCommand.info(buildText {
                            styled(Color.GRAY) {
                                literal("$name ")
                            }
                            styled(if (module.isEnabled) Color.GREEN else Color.RED) {
                                literal(if (module.isEnabled) "enabled" else "disabled")
                            }
                        })
                        success()
                    } ?: failure("Failed to ${if (module.isEnabled) "enable" else "disable"} module $name")
                }
            }
        }
    }
}
