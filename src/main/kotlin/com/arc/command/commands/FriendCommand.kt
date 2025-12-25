
package com.arc.command.commands

import com.arc.Arc.mc
import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.literal
import com.arc.brigadier.argument.string
import com.arc.brigadier.argument.uuid
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.command.ArcCommand
import com.arc.config.configurations.FriendConfig
import com.arc.friend.FriendManager
import com.arc.network.mojang.getProfile
import com.arc.util.Communication.info
import com.arc.util.extension.CommandBuilder
import com.arc.util.text.ClickEvents
import com.arc.util.text.buildText
import com.arc.util.text.literal
import com.arc.util.text.styled
import kotlinx.coroutines.runBlocking
import java.awt.Color

object FriendCommand : ArcCommand(
    name = "friends",
    usage = "friends <add | remove> <name | uuid>",
    description = "Add or remove a friend"
) {
    override fun CommandBuilder.create() {
        execute {
            this@FriendCommand.info(
                buildText {
                    if (FriendManager.friends.isEmpty()) {
                        literal("You have no friends yet. Go make some! :3\n")
                    } else {
                        literal("Your friends (${FriendManager.friends.size}):\n")

                        FriendManager.friends.forEachIndexed { index, gameProfile ->
                            literal("   ${index + 1}. ${gameProfile.name}\n")
                        }
                    }

                    literal("\n")
                    styled(
                        color = Color.CYAN,
                        underlined = true,
                        clickEvent = ClickEvents.openFile(FriendConfig.primary.path),
                    ) {
                        literal("Click to open your friends list as a file")
                    }
                }
            )
        }

        required(literal("add")) {
            required(string("player name")) { player ->
                suggests { _, builder ->
                    mc.networkHandler
                        ?.playerList
                        ?.filter { it.profile != mc.gameProfile }
                        ?.map { it.profile.name }
                        ?.forEach { builder.suggest(it) }

                    builder.buildFuture()
                }

                executeWithResult {
                    runBlocking {
                        val name = player().value()

                        if (mc.gameProfile.name == name) return@runBlocking failure("You can't befriend yourself")

                        val profile = mc.networkHandler
                            ?.playerList
                            ?.map { it.profile }
                            ?.firstOrNull { it.name == name }
                            ?: getProfile(name)
                                .getOrElse { return@runBlocking failure("Could not find the player") }

                        return@runBlocking if (FriendManager.befriend(profile)) {
                            info(FriendManager.befriendedText(profile.name))
                            success()
                        } else {
                            failure("This player is already in your friend list")
                        }
                    }
                }
            }

            required(uuid("player uuid")) { player ->
                suggests { _, builder ->
                    mc.networkHandler
                        ?.playerList
                        ?.filter { it.profile != mc.gameProfile }
                        ?.map { it.profile.id }
                        ?.forEach { builder.suggest(it.toString()) }

                    builder.buildFuture()
                }

                executeWithResult {
                    runBlocking {
                        val uuid = player().value()

                        if (mc.gameProfile.id == uuid) return@runBlocking failure("You can't befriend yourself")

                        val profile = mc.networkHandler
                            ?.playerList
                            ?.map { it.profile }
                            ?.firstOrNull { it.id == uuid }
                            ?: getProfile(uuid)
                                .getOrElse { return@runBlocking failure("Could not find the player") }

                        return@runBlocking if (FriendManager.befriend(profile)) {
                            this@FriendCommand.info(FriendManager.befriendedText(profile.name))
                            success()
                        } else {
                            failure("This player is already in your friend list")
                        }
                    }
                }
            }
        }

        required(literal("remove")) {
            required(string("player name")) { player ->
                suggests { _, builder ->
                    FriendManager.friends.map { it.name }
                        .forEach { builder.suggest(it) }

                    builder.buildFuture()
                }

                executeWithResult {
                    val name = player().value()
                    val profile = FriendManager.gameProfile(name)
                        ?: return@executeWithResult failure("This player is not in your friend list")

                    return@executeWithResult if (FriendManager.unfriend(profile)) {
                        this@FriendCommand.info(FriendManager.unfriendedText(name))
                        success()
                    } else {
                        failure("This player is not in your friend list")
                    }
                }
            }
        }
    }
}
