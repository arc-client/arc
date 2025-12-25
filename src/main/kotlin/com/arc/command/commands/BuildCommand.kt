
package com.arc.command.commands

import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.greedyString
import com.arc.brigadier.argument.literal
import com.arc.brigadier.argument.value
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.config.AutomationConfig
import com.arc.interaction.construction.StructureRegistry
import com.arc.interaction.construction.blueprint.Blueprint.Companion.toStructure
import com.arc.interaction.construction.blueprint.StaticBlueprint.Companion.toBlueprint
import com.arc.task.RootTask.run
import com.arc.task.tasks.BuildTask
import com.arc.task.tasks.BuildTask.Companion.build
import com.arc.threading.runSafe
import com.arc.util.Communication.info
import com.arc.util.extension.CommandBuilder
import com.arc.util.extension.move
import java.nio.file.InvalidPathException
import java.nio.file.NoSuchFileException
import java.nio.file.Path

object BuildCommand : ArcCommand(
    name = "Build",
    description = "Builds a structure",
    usage = "build <structure>"
) {
    private var lastBuildTask: BuildTask? = null

    override fun CommandBuilder.create() {
        required(literal("place")) {
            required(greedyString("structure")) { structure ->
                suggests { _, builder ->
                    StructureRegistry.forEach { key, _ -> builder.suggest(key) }
                    builder.buildFuture()
                }
                executeWithResult {
                    val pathString = structure().value()
                    runSafe<Unit> {
                        try {
                            StructureRegistry
                                .loadStructureByRelativePath(Path.of(pathString))
                                .let { template ->
                                    info("Building structure $pathString with dimensions ${template.size.toShortString()} created by ${template.author}")
                                    lastBuildTask = with(AutomationConfig.Companion.DEFAULT) {
                                        template.toStructure()
                                            .move(player.blockPos)
                                            .toBlueprint()
                                            .build()
                                            .run()
                                    }

                                    return@executeWithResult success()
                                }
                        } catch (e: InvalidPathException) {
                            return@executeWithResult failure("Invalid path $pathString")
                        } catch (e: NoSuchFileException) {
                            return@executeWithResult failure("Structure $pathString not found")
                        } catch (e: Exception) {
                            return@executeWithResult failure(
                                e.message ?: "Failed to load structure $pathString"
                            )
                        }
                    }

                    failure("Structure $pathString not found")
                }
            }
        }

        required(literal("cancel")) {
            executeWithResult {
                lastBuildTask?.cancel() ?: run {
                    return@executeWithResult failure("No build task to cancel")
                }
                this@BuildCommand.info("$lastBuildTask cancelled")
                lastBuildTask = null
                success()
            }
        }
    }
}
