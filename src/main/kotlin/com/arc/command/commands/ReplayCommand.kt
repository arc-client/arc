
package com.arc.command.commands

import com.google.gson.JsonSyntaxException
import com.arc.brigadier.CommandResult
import com.arc.brigadier.argument.greedyString
import com.arc.brigadier.argument.integer
import com.arc.brigadier.argument.literal
import com.arc.brigadier.argument.value
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.module.modules.player.Replay
import com.arc.util.FileUtils.listRecursive
import com.arc.util.FolderRegister
import com.arc.util.extension.CommandBuilder
import kotlin.io.path.exists

object ReplayCommand : ArcCommand(
    name = "replay",
    usage = "replay <play | load | save | prune>",
    description = "Play, load, save, or prune a replay"
) {
    override fun CommandBuilder.create() {
        required(literal("play")) {
            required(integer("index")) { index ->
                executeWithResult {
                    Replay.playRecording(index().value())
                }
            }
        }

        required(literal("load")) {
            required(greedyString("replay filepath")) { replayName ->
                suggests { _, builder ->
                    val dir = FolderRegister.replay.toFile()
                    dir.listRecursive { it.isFile }.forEach {
                        builder.suggest(it.relativeTo(dir).path)
                    }
                    builder.buildFuture()
                }

                executeWithResult {
                    val replayFile = FolderRegister.replay.resolve(replayName().value())

                    if (!replayFile.exists()) {
                        return@executeWithResult CommandResult.failure("Replay file does not exist")
                    }

                    try {
                        Replay.loadRecording(replayFile.toFile())
                    } catch (e: JsonSyntaxException) {
                        return@executeWithResult CommandResult.failure("Failed to load replay file: ${e.message}")
                    }

                    CommandResult.success()
                }
            }
        }
        required(literal("save")) {
            required(integer("id")) { id ->
                required(greedyString("replay name")) { replayName ->
                    executeWithResult {
                        Replay.saveRecording(id().value(), replayName().value())
                    }
                }
            }
        }
        required(literal("prune")) {
            required(integer("id")) { id ->
                executeWithResult {
                    Replay.pruneRecording(id().value())
                }
            }
        }
    }
}
