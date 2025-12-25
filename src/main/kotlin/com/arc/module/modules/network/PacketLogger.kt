
package com.arc.module.modules.network

import com.arc.Arc
import com.arc.Arc.mc
import com.arc.event.events.PacketEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.UnsafeListener.Companion.listenUnsafe
import com.arc.event.listener.UnsafeListener.Companion.listenConcurrentlyUnsafe
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.threading.runIO
import com.arc.util.Communication
import com.arc.util.Communication.info
import com.arc.util.DynamicReflectionSerializer.dynamicString
import com.arc.util.FolderRegister
import com.arc.util.FolderRegister.relativeMCPath
import com.arc.util.Formatting.getTime
import com.arc.util.text.ClickEvents
import com.arc.util.text.buildText
import com.arc.util.text.clickEvent
import com.arc.util.text.color
import com.arc.util.text.literal
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.minecraft.network.packet.Packet
import java.awt.Color
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.io.path.pathString

object PacketLogger : Module(
    name = "PacketLogger",
    description = "Serializes network traffic and persists it for later analysis",
    tag = ModuleTag.NETWORK,
    autoDisable = true
) {
    private val logToChat by setting("Log To Chat", false, "Log packets to chat")

    // ToDo: Implement HUD logging when HUD is done
//    private val logToHUD by setting("Log To HUD", false, "Log packets to HUD")
    private val networkSide by setting("Network Side", NetworkSide.Any, "Side of the network to log packets from")
    private val logTicks by setting("Log Ticks", true, "Show game ticks in the log")
    private val scope by setting("Scope", Scope.Any, "Scope of packets to log")

//    val packetList = getInstances<Packet<*>>()
    // ToDo: Add a packet list
    //private val whitelist by setting<String>("Whitelist Packets", emptyList<String>(), emptyList<String>(), "Packets to whitelist", { JsonPrimitive(it) }, { it.asString }) { scope == Scope.Whitelist }
    //private val blacklist by setting<String>("Blacklist Packets", emptyList<String>(), emptyList<String>(), "Packets to blacklist", { JsonPrimitive(it) }, { it.asString }) { scope == Scope.Blacklist }
    private val maxRecursionDepth by setting("Max Recursion Depth", 6, 1..10, 1, "Maximum recursion depth for packet serialization")
    private val logConcurrent by setting("Build Data Concurrent", false, "Whether to serialize packets concurrently. Will not save packets in chronological order but wont lag the game.")

    private var file: File? = null
    private val entryFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSS")
    private val fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS")

    enum class NetworkSide {
        Any, Client, Server;

        fun shouldLog(networkSide: NetworkSide) =
            this == Any || this == networkSide
    }

    enum class Scope {
        Any, Whitelist, Blacklist;

        fun shouldLog(packet: Packet<*>) = when (this) {
            Any -> true
            Whitelist -> false//packet::class.simpleName in whitelist
            Blacklist -> false//packet::class.simpleName !in blacklist
        }
    }

    private val storageFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        runIO {
            storageFlow.collect { entry ->
                file?.appendText(entry)
                if (logToChat) this@PacketLogger.info(entry)
            }
        }

        onEnableUnsafe {
            val fileName = "packet-log-${getTime(fileFormatter)}.txt"

            // ToDo: Organize files with FolderRegister.worldBoundDirectory
            file = FolderRegister.packetLogs.resolve(fileName).toFile().apply {
                if (!parentFile.exists()) {
                    parentFile.mkdirs()
                }
                if (!exists()) {
                    createNewFile()
                }
                val info = buildText {
                    clickEvent(ClickEvents.openFile(relativeMCPath.pathString)) {
                        literal("Packet logger started: ")
                        color(Color.YELLOW) { literal(fileName) }
                        literal(" (click to open)")
                    }
                }
                this@PacketLogger.info(info)
            }.apply {
                StringBuilder().apply {
                    appendLine(Communication.ascii)
                    appendLine("${Arc.SYMBOL} - Arc ${Arc.VERSION} - Packet Log")

                    val playerName = mc.player?.name?.string ?: "Unknown"
                    appendLine("Started at ${getTime()} by $playerName")
                    when {
                        mc.isIntegratedServerRunning -> {
                            appendLine("Integrated server running.")
                        }

                        mc.currentServerEntry != null -> {
                            appendLine("Connected to ${mc.currentServerEntry?.name} at ${mc.currentServerEntry?.address}.")
                        }

                        else -> {
                            appendLine("Started in Main Menu")
                        }
                    }
                    appendLine()
                }.toString().let { header ->
                    storageFlow.tryEmit(header)
                }
            }
        }

        onDisableUnsafe {
            file?.let {
                val info = buildText {
                    literal("Stopped logging packets to ")
                    clickEvent(ClickEvents.openFile(it.relativeMCPath.pathString)) {
                        color(Color.YELLOW) { literal(it.relativeMCPath.pathString) }
                        literal(" (click to open)")
                    }
                }
                this@PacketLogger.info(info)

                file = null
            }
        }

        listenUnsafe<TickEvent.Pre> {
            if (!logTicks) return@listenUnsafe

            storageFlow.tryEmit("Started tick at ${getTime(entryFormatter)}\n\n")
        }

        listenUnsafe<PacketEvent.Receive.Pre> {
            if (logConcurrent
                || !scope.shouldLog(it.packet)
                || !networkSide.shouldLog(NetworkSide.Server)
            ) return@listenUnsafe

            it.packet.logReceived()
        }

        listenUnsafe<PacketEvent.Send.Pre> {
            if (logConcurrent
                || !scope.shouldLog(it.packet)
                || !networkSide.shouldLog(NetworkSide.Client)
            ) return@listenUnsafe


            it.packet.logSent()
        }

        listenConcurrentlyUnsafe<PacketEvent.Receive.Pre> {
            if (!logConcurrent
                || !scope.shouldLog(it.packet)
                || !networkSide.shouldLog(NetworkSide.Server)
            ) return@listenConcurrentlyUnsafe

            it.packet.logReceived()
        }

        listenConcurrentlyUnsafe<PacketEvent.Send.Pre> {
            if (!logConcurrent
                || !scope.shouldLog(it.packet)
                || !networkSide.shouldLog(NetworkSide.Client)
            ) return@listenConcurrentlyUnsafe

            it.packet.logSent()
        }
    }

    private fun Packet<*>.logReceived() {
        storageFlow.tryEmit("Received at ${getTime(entryFormatter)}\n${dynamicString(maxRecursionDepth)}\n")
    }

    private fun Packet<*>.logSent() {
        storageFlow.tryEmit("Sent at ${getTime(entryFormatter)}\n${dynamicString(maxRecursionDepth)}\n")
    }
}
