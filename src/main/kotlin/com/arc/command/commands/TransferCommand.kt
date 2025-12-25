
package com.arc.command.commands

import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.integer
import com.arc.brigadier.argument.itemStack
import com.arc.brigadier.argument.literal
import com.arc.brigadier.argument.string
import com.arc.brigadier.argument.value
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.config.AutomationConfig
import com.arc.interaction.material.StackSelection.Companion.selectStack
import com.arc.interaction.material.container.ContainerManager
import com.arc.interaction.material.container.ContainerManager.findContainersWithMaterial
import com.arc.interaction.material.container.ContainerManager.findContainersWithSpace
import com.arc.interaction.material.transfer.TransferResult
import com.arc.task.RootTask.run
import com.arc.threading.runSafe
import com.arc.util.Communication.info
import com.arc.util.extension.CommandBuilder

object TransferCommand : ArcCommand(
    name = "transfer",
    usage = "transfer <move | cancel | undo> <item> <amount> <to>",
    description = "Transfer items from anywhere to anywhere",
) {
    private var lastContainerTransfer: TransferResult.ContainerTransfer? = null

    override fun CommandBuilder.create() {
        required(itemStack("stack", registry)) { stack ->
            required(integer("amount", 1)) { amount ->
                required(string("from")) { from ->
                    suggests { ctx, builder ->
                        val count = amount(ctx).value()
                        val selection = selectStack(count) {
                            isItem(stack(ctx).value().item)
                        }
                        with(AutomationConfig.Companion.DEFAULT) {
                            runSafe {
                                selection.findContainersWithMaterial().forEachIndexed { i, container ->
                                    builder.suggest("\"${i + 1}. ${container.name}\"", container.description(selection))
                                }
                            }
                        }
                        builder.buildFuture()
                    }
                    required(string("to")) { to ->
                        suggests { ctx, builder ->
                            val selection = selectStack(amount(ctx).value()) {
                                isItem(stack(ctx).value().item)
                            }
                            with(AutomationConfig.Companion.DEFAULT) {
                                runSafe {
                                    findContainersWithSpace(selection).forEachIndexed { i, container ->
                                        builder.suggest("\"${i + 1}. ${container.name}\"", container.description(selection))
                                    }
                                }
                            }
                            builder.buildFuture()
                        }
                        executeWithResult {
                            val selection = selectStack(amount().value()) {
                                isItem(stack().value().item)
                            }
                            val fromContainer = ContainerManager.containers().find {
                                it.name == from().value().split(".").last().trim()
                            } ?: return@executeWithResult failure("From container not found")

                            val toContainer = ContainerManager.containers().find {
                                it.name == to().value().split(".").last().trim()
                            } ?: return@executeWithResult failure("To container not found")

                            with(AutomationConfig.Companion.DEFAULT) {
                                when (val transaction = fromContainer.transfer(selection, toContainer)) {
                                    is TransferResult.ContainerTransfer -> {
                                        info("${transaction.name} started.")
                                        lastContainerTransfer = transaction
                                        transaction.finally {
                                            info("${transaction.name} completed.")
                                        }.run()
                                        return@executeWithResult success()
                                    }

                                    is TransferResult.MissingItems -> {
                                        return@executeWithResult failure("Missing items: ${transaction.missing}")
                                    }

                                    is TransferResult.NoSpace -> {
                                        return@executeWithResult failure("No space in ${toContainer.name}")
                                    }
                                }
                            }

                            return@executeWithResult success()
                        }
                    }
                }
            }
        }

        required(literal("cancel")) {
            executeWithResult {
                lastContainerTransfer?.cancel() ?: run {
                    return@executeWithResult failure("No transfer to cancel")
                }
                this@TransferCommand.info("$lastContainerTransfer cancelled")
                lastContainerTransfer = null
                success()
            }
        }
    }
}
