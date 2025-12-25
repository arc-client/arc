
package com.arc.module.hud

import com.arc.gui.dsl.ImGuiBuilder
import com.arc.interaction.managers.DebugLogger
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.util.NamedEnum

@Suppress("Unused")
object ManagerDebugLoggers : HudModule(
    "ManagerDebugLoggers",
    "debug loggers for all action managers in arc",
    ModuleTag.HUD,
    customWindow = true
) {
    enum class Group(override val displayName: String) : NamedEnum {
        General("General"),
        Break("Break"),
        Place("Place"),
        Interact("Interact"),
        Rotation("Rotation"),
        Hotbar("Hotbar"),
        Inventory("Inventory")
    }

    private val loggers = mutableMapOf<() -> Boolean, DebugLogger>()

    val autoScroll by setting("Auto-Scroll", true, "Locks the page at the bottom of the logs").group(Group.General)
    val wrapText by setting("Wrap Text", true, "Wraps the logs to new lines if they go off the panel").group(Group.General)
    val showDebug by setting("Show Debug", true, "Shows debug logs").group(Group.General)
    val showSuccess by setting("Show Success", true, "Shows success logs").group(Group.General)
    val showWarning by setting("Show Warning", true, "Shows warning logs").group(Group.General)
    val showError by setting("Show Error", true, "Shows error logs").group(Group.General)
    val showSystem by setting("Show System", true, "Shows system logs").group(Group.General)
    val maxLogEntries by setting("Max Log Entries", 100, 1..1000, 1, "Maximum amount of entries in the log").group(Group.General)
        .onValueChange { from, to ->
            if (to < from) {
                loggers.values.forEach { logger ->
                    while(logger.logs.size > to) {
                        logger.logs.removeFirst()
                    }
                }
            }
        }

    private val showBreakManager by setting("Show Break Manager Logger", false).group(Group.Break)
    val breakManagerLogger = DebugLogger("Break Manager Logger").store { showBreakManager }

    private val showPlaceManager by setting("Show Place Manager Logger", false).group(Group.Place)
    val placeManagerLogger = DebugLogger("Place Manager Logger").store { showPlaceManager }

    private val showInteractionManager by setting("Show Interaction Manager Logger", false).group(Group.Interact)
    val interactionManagerLogger = DebugLogger("Interaction Manager Logger").store { showInteractionManager }

    private val showRotationManager by setting("Show Rotation Manager Logger", false).group(Group.Rotation)
    val rotationManagerLogger = DebugLogger("Rotation Manager Logger").store { showRotationManager }

    private val showHotbarManager by setting("Show Hotbar Manager Logger", false).group(Group.Hotbar)
    val hotbarManagerLogger = DebugLogger("Hotbar Manager Logger").store { showHotbarManager }

    private val showInventoryManager by setting("Show Inventory Manager Logger", false).group(Group.Inventory)
    val inventoryManagerLogger = DebugLogger("Inventory Manager Logger").store { showInventoryManager }

    private fun DebugLogger.store(show: () -> Boolean) =
        also { loggers.put(show, this) }

    override fun ImGuiBuilder.buildLayout() {
        loggers.entries.forEach { entry ->
            if (entry.key()) with(entry.value) {
                buildLayout()
            }
        }
    }
}