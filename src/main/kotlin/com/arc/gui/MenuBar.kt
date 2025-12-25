
package com.arc.gui

import com.arc.Arc
import com.arc.Arc.REPO_URL
import com.arc.Arc.mc
import com.arc.command.CommandRegistry
import com.arc.config.AutomationConfig
import com.arc.config.Configuration
import com.arc.config.Configuration.Companion.configurables
import com.arc.config.UserAutomationConfig
import com.arc.config.configurations.UserAutomationConfigs
import com.arc.core.Loader
import com.arc.event.EventFlow
import com.arc.graphics.texture.TextureOwner.upload
import com.arc.gui.DearImGui.EXTERNAL_LINK
import com.arc.gui.components.ClickGuiLayout
import com.arc.gui.components.HudGuiLayout
import com.arc.gui.components.QuickSearch
import com.arc.gui.components.SettingsWidget.buildConfigSettingsContext
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.interaction.BaritoneManager
import com.arc.module.ModuleRegistry
import com.arc.module.ModuleRegistry.moduleNameMap
import com.arc.module.tag.ModuleTag
import com.arc.network.ArcAPI
import com.arc.threading.runSafe
import com.arc.util.Communication.info
import com.arc.util.Diagnostics.gatherDiagnostics
import com.arc.util.FolderRegister
import com.arc.util.FolderRegister.minecraft
import com.mojang.blaze3d.platform.TextureUtil
import imgui.ImGui
import imgui.ImGui.closeCurrentPopup
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Util
import net.minecraft.world.GameMode
import java.util.*

object MenuBar {
    private var aboutRequested = false
    var newConfigName = ""
    val headerLogo = upload("textures/arc_text_color.png")
    val arcLogo = upload("textures/arc.png")
    val githubLogo = upload("textures/github_logo.png")

    var height = 0f

    fun ImGuiBuilder.buildMenuBar() {
        mainMenuBar {
            height = windowHeight

            arcMenu()
            menu("HUD") { buildHudMenu() }
            menu("GUI") { buildGuiMenu() }
            menu("Modules") { buildModulesMenu() }
            menu("Automation Configs") { buildAutomationConfigsMenu() }
            menu("Minecraft") { buildMinecraftMenu() }
            menu("Help") { buildHelpMenu() }
            buildGitHubReference()
        }

        if (aboutRequested) {
            ImGui.openPopup("About Arc")
            aboutRequested = false
        }

        aboutPopup()
    }

    private fun ImGuiBuilder.arcMenu() {
        ImGui.pushStyleColor(ImGuiCol.Text, 0)
        val opened = ImGui.beginMenu("Arc")

        val headerW = itemRectMaxX - itemRectMinX
        val headerH = itemRectMaxY - itemRectMinY

        val pad = 2f
        val arcIconSize = (headerH - pad * 2f).coerceAtLeast(1f)
        val iconX = itemRectMinX + (headerW - arcIconSize) * 0.5f
        val iconY = itemRectMinY + (headerH - arcIconSize) * 0.5f

        foregroundDrawList.addImage(
            arcLogo.id.toLong(),
            iconX, iconY,
            iconX + arcIconSize, iconY + arcIconSize
        )
        ImGui.popStyleColor()

        if (opened) {
            buildArcMenu()
            ImGui.endMenu()
        }
    }

    private fun ImGuiBuilder.buildArcMenu() {
        menu("Save Config...") {
            menuItem("Save All Configs") {
                Configuration.configurations.forEach { it.trySave(true) }
                info("Saved ${Configuration.configurations.size} configuration files.")
            }
            Configuration.configurations.forEach { config ->
                menuItem("Save ${config.configName}") {
                    config.trySave(true)
                    info("Saved ${config.configName}")
                }
            }
        }
        menu("Load Config...") {
            menuItem("Load All Configs") {
                Configuration.configurations.forEach { it.tryLoad() }
                info("Loaded ${Configuration.configurations.size} configuration files.")
            }
            Configuration.configurations.forEach { config ->
                menuItem("Load ${config.configName}") {
                    config.tryLoad()
                    info("Loaded ${config.configName}")
                }
            }
        }
        separator()
        menu("Settings") {
            menu("HUD Settings") {
                buildConfigSettingsContext(HudGuiLayout)
            }
            menu("GUI Settings") {
                buildConfigSettingsContext(ClickGuiLayout)
            }
            menu("Arc API Settings") {
                buildConfigSettingsContext(ArcAPI)
            }
            menu("Baritone Settings") {
                buildConfigSettingsContext(BaritoneManager)
            }
        }
        separator()
        menu("Open Folder") {
            menuItem("Open Arc Folder") {
                Util.getOperatingSystem().open(FolderRegister.arc)
            }
            menuItem("Open Config Folder") {
                Util.getOperatingSystem().open(FolderRegister.config)
            }
            menuItem("Open Packet Logs Folder") {
                Util.getOperatingSystem().open(FolderRegister.packetLogs)
            }
            menuItem("Open Replay Folder") {
                Util.getOperatingSystem().open(FolderRegister.replay)
            }
            menuItem("Open Cache Folder") {
                Util.getOperatingSystem().open(FolderRegister.cache)
            }
            menuItem("Open Capes Folder") {
                Util.getOperatingSystem().open(FolderRegister.capes)
            }
            menuItem("Open Structures Folder") {
                Util.getOperatingSystem().open(FolderRegister.structure)
            }
            menuItem("Open Maps Folder") {
                Util.getOperatingSystem().open(FolderRegister.maps)
            }
        }
        separator()
        menuItem("New Profile...", enabled = false) {
            // ToDo (New Profile):
            //  - Open a modal "New Profile" with:
            //      [Profile Name] text input
            //      [Template] combo: Empty / Recommended Defaults / Copy from Current
            //      [Include HUD Layout] checkbox
            //  - On Create: instantiate and activate the profile, optionally copying values from current.
            //  - On Cancel: close modal with no changes.
        }
        menuItem("Import Profile...", enabled = false) {
            // ToDo (Import Profile):
            //  - Show a file picker for profile file(s).
            //  - Preview dialog: profile name, version, module count, settings count, includes HUD?
            //  - Provide options: Merge into Current / Replace Current.
            //  - Apply with progress/rollback on failure; toast result.
        }
        menuItem("Export Current Profile...", enabled = false) {
            // ToDo (Export Profile):
            //  - File save modal with checkboxes:
            //      [Include HUD Layout] [Include Keybinds] [Include Backups Metadata]
            //  - Create the export and toast result.
        }
        menu("Recent Profiles") {
            // ToDo (MRU Profiles):
            //  - Populate from a most-recently-used (MRU) list persisted in preferences.
            //  - On click: switch active profile (confirm if unsaved changes).
            menuItem("Example Profile", enabled = false) {}
        }
        separator()
        menu("Autosave Settings") {
            // ToDo:
            //  - Toggle autosave, set interval (1..60s), backup rotation count (0..20).
            menuItem("Autosave on changes", selected = true, enabled = false) {}
            menuItem("Autosave Interval: 10s", enabled = false) {}
            menuItem("Rotate Backups: 5", enabled = false) {}
        }
        menu("Backup & Restore") {
            // ToDo:
            //  - “Create Backup Now” and “Manage/Restore Backups” UIs; list with timestamps/comments.
            menuItem("Create Backup Now", enabled = false) {}
            menuItem("Restore From Backup...", enabled = false) {}
            menuItem("Manage Backups...", enabled = false) {}
        }
        menuItem("Profiles & Scopes...", enabled = false) {
            // ToDo (Profiles & Scopes Window):
            //  - Active Profile dropdown.
            //  - Scopes: Global / Per-Server / Per-World with enable overrides.
            //  - Show overridden-only list, origin badges, and precedence explanation.
        }
        separator()
        menuItem("About...") {
            aboutRequested = true
        }
        menuItem("Developer Mode", selected = ClickGuiLayout.developerMode) {
            ClickGuiLayout.developerMode = !ClickGuiLayout.developerMode
        }
        separator()
        menuItem("Close GUI", "Esc") { ArcScreen.close() }
        menuItem("Exit Client") { mc.scheduleStop() }
    }

    private fun ImGuiBuilder.buildHudMenu() {
        menuItem(if (HudGuiLayout.isLocked) "Unlock" else "Lock") {
            HudGuiLayout.isLocked = !HudGuiLayout.isLocked
        }
        menuItem(if (HudGuiLayout.isShownInGUI) "Hide" else "Show") {
            HudGuiLayout.isShownInGUI = !HudGuiLayout.isShownInGUI
        }
        separator()
        menu("HUD Settings") {
            buildConfigSettingsContext(HudGuiLayout)
        }
    }

    private fun ImGuiBuilder.buildGuiMenu() {
        buildConfigSettingsContext(ClickGuiLayout)
    }

    private fun ImGuiBuilder.buildModulesMenu() {
        menu("Module Tag") {
            ModuleTag.defaults.forEach { tag ->
                menuItem(tag.name, selected = ModuleTag.isTagShown(tag)) {
                    ModuleTag.toggleTag(tag)
                }
            }
        }
        separator()
        // By Tag → quick enable/disable per module
        ModuleTag.defaults.forEach { tag ->
            menu(tag.name) {
                ModuleRegistry.modules
                    .filter { it.tag == tag }
                    .forEach { module ->
                        menuItem(module.name, selected = module.isEnabled) {
                            if (module.isEnabled) module.disable() else module.enable()
                        }
                        // Optionally, offer a "Settings..." item to focus this module’s details UI.
                    }
            }
        }
    }

    private fun ImGuiBuilder.buildAutomationConfigsMenu() {
        button("New Config") { ImGui.openPopup("##new-config") }
        popupContextWindow("##new-config") {
            inputText("Name", ::newConfigName)
            button("Create") {
                if (newConfigName.isEmpty() && configurables.none { it.name == newConfigName }) return@button
                UserAutomationConfig(newConfigName)
                newConfigName = ""
                closeCurrentPopup()
                return@button
            }
            sameLine()
            button("Cancel") {
                newConfigName = ""
                closeCurrentPopup()
            }
        }

        UserAutomationConfigs.configurables.forEach { config ->
            if (config !is UserAutomationConfig) throw java.lang.IllegalStateException("All configurables within UserAutomationConfigs must be UserAutomationConfigs!")
            buildAutomationConfigSelectable(config)
        }
        buildAutomationConfigSelectable(AutomationConfig.Companion.DEFAULT)
    }

    private fun ImGuiBuilder.buildAutomationConfigSelectable(config: AutomationConfig) {
	    ImGui.setNextWindowSizeConstraints(0f, 0f, Float.MAX_VALUE, io.displaySize.y * 0.5f)
		menu(config.name) {
			if (config is UserAutomationConfig) {
				with(config.linkedModules) { buildLayout() }
				button("Delete") {
					config.linkedModules.value.forEach {
						moduleNameMap[it]?.let { module ->
							module.automationConfig = module.defaultAutomationConfig
						}
					}
					UserAutomationConfigs.configurables.remove(config)
				}
				separator()
			}
			buildConfigSettingsContext(config)
		}
    }

    private fun ImGuiBuilder.buildMinecraftMenu() {
        menu("Open Folder") {
            menuItem("Open Minecraft Folder") {
                Util.getOperatingSystem().open(minecraft)
            }
            menuItem("Open Saves Folder") {
                Util.getOperatingSystem().open(mc.runDirectory.toPath().toAbsolutePath().resolve("saves").toFile())
            }
            menuItem("Open Screenshots Folder") {
                Util.getOperatingSystem().open(mc.runDirectory.toPath().toAbsolutePath().resolve("screenshots").toFile())
            }
            menuItem("Open Resource Packs Folder") {
                Util.getOperatingSystem().open(mc.runDirectory.toPath().toAbsolutePath().resolve("resourcepacks").toFile())
            }
            menuItem("Open Mods Folder") {
                Util.getOperatingSystem().open(mc.runDirectory.toPath().toAbsolutePath().resolve("mods").toFile())
            }
        }
        separator()
        runSafe {
            menu("Gamemode", enabled = player.hasPermissionLevel(2)) {
                menuItem("Survival", selected = interaction.gameMode == GameMode.SURVIVAL) {
                    connection.sendCommand("gamemode survival")
                }
                menuItem("Creative", selected = interaction.gameMode == GameMode.CREATIVE) {
                    connection.sendCommand("gamemode creative")
                }
                menuItem("Adventure", selected = interaction.gameMode == GameMode.ADVENTURE) {
                    connection.sendCommand("gamemode adventure")
                }
                menuItem("Spectator", selected = interaction.gameMode == GameMode.SPECTATOR) {
                    connection.sendCommand("gamemode spectator")
                }
            }
            menu("Debug Menu") {
                menuItem("Show Advanced Tooltips", "F3+H", mc.options.advancedItemTooltips) {
                    mc.options.advancedItemTooltips = !mc.options.advancedItemTooltips
                    mc.options.write()
                }
                menuItem("Show Chunk Borders", "F3+G", mc.debugRenderer.showChunkBorder) {
                    mc.debugRenderer.toggleShowChunkBorder()
                }
                menuItem("Show Octree", selected = mc.debugRenderer.showOctree) {
                    mc.debugRenderer.toggleShowOctree()
                }
                menuItem("Show Hitboxes", "F3+B", mc.entityRenderDispatcher.shouldRenderHitboxes()) {
                    val now = !mc.entityRenderDispatcher.shouldRenderHitboxes()
                    mc.entityRenderDispatcher.setRenderHitboxes(now)
                }
                menuItem("Copy Location (as command)", "F3+C") {
                    val cmd = String.format(
                        Locale.ROOT,
                        "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
                        world.registryKey.value,
                        player.x, player.y, player.z, player.yaw, player.pitch
                    )
                    ImGui.setClipboardText(cmd)
                    info("Copied location command to clipboard.")
                }
                menuItem("Clear Chat", "F3+D") {
                    mc.inGameHud?.chatHud?.clear(false)
                }

                separator()

                menuItem(
                    label = "Pause On Lost Focus",
                    shortcut = "F3+Esc",
                    selected = mc.options.pauseOnLostFocus
                ) {
                    mc.options.pauseOnLostFocus = !mc.options.pauseOnLostFocus
                    mc.options.write()
                    info("Pause on lost focus ${if (mc.options.pauseOnLostFocus) "enabled" else "disabled"}.")
                }

                separator()

                menuItem("Reload Resource Packs", "F3+T") {
                    info("Reloading resource packs...")
                    mc.reloadResources()
                }

                menuItem("Reload Chunks", "F3+A") {
                    mc.worldRenderer.reload()
                }

                separator()

                menuItem("Show Debug Menu", "F3", mc.debugHud.showDebugHud) {
                    mc.debugHud.toggleDebugHud()
                }
                menuItem("Rendering Chart", "F3+1", mc.debugHud.renderingChartVisible) {
                    mc.debugHud.toggleRenderingChart()
                }
                menuItem("Rendering & Tick Charts", "F3+2", mc.debugHud.renderingAndTickChartsVisible) {
                    mc.debugHud.toggleRenderingAndTickCharts()
                }
                menuItem("Packet Size & Ping Charts", "F3+3", mc.debugHud.packetSizeAndPingChartsVisible) {
                    mc.debugHud.togglePacketSizeAndPingCharts()
                }

                separator()

                menuItem("Start/Stop Profiler", "F3+L") {
                    mc.toggleDebugProfiler { message ->
                        info(message)
                    }
                }
                menuItem("Dump Dynamic Textures", "F3+S") {
                    val root = mc.runDirectory.toPath().toAbsolutePath()
                    val output = TextureUtil.getDebugTexturePath(root)
                    mc.textureManager.dumpDynamicTextures(output)
                    info("Dumped dynamic textures to: ${root.relativize(output)}")
                }
            }
        } ?: menuItem("Debug (only available ingame)", enabled = false)
    }

    private fun ImGuiBuilder.buildHelpMenu() {
        menuItem("Quick Search...", "Shift+Shift") {
            QuickSearch.open()
        }
        menuItem("Documentation $EXTERNAL_LINK") {
            Util.getOperatingSystem().open("$REPO_URL/wiki")
        }
        menuItem("Report Issue $EXTERNAL_LINK") {
            mc.keyboard.clipboard = gatherDiagnostics()
            info("Copied diagnostics to clipboard. Please paste it in a new issue on GitHub and click “Submit new issue”. Thank you!")
            Util.getOperatingSystem().open("$REPO_URL/issues")
        }
        menuItem("Check for Updates $EXTERNAL_LINK") {
            // ToDo:
            //  - Check for a newer version, show availability & changelog, and allow opening release page.
            //  - Needs UpdateManager
            Util.getOperatingSystem().open("$REPO_URL/releases")
        }
    }

    private fun ImGuiBuilder.aboutPopup() {
        popupModal("About Arc", ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar) {
            imageHorizontallyCentered(headerLogo.id.toLong(), 553f, 200f)
            group {
                text("Version: ${Arc.VERSION}")
                if (Arc.isDebug) text("Development Environment")
                text("Runtime: ${Loader.runtime}")
                text("Modules: ${ModuleRegistry.modules.size}")
                text("Commands: ${CommandRegistry.commands.size}")
                val totalSettings = Configuration.configurations.sumOf { cfg ->
                    cfg.configurables.sumOf { it.settings.size }
                }
                text("Settings: $totalSettings")
                text("Synchronous listeners: ${EventFlow.syncListeners.size}")
                text("Concurrent listeners: ${EventFlow.concurrentListeners.size}")
            }
            separator()
            text("Authors")
            FabricLoader.getInstance().getModContainer("arc")
                .orElseThrow { IllegalStateException("Could not find Arc mod container!") }
                .metadata
                .authors
                .forEach { author ->
                    if (author.name.isEmpty()) return@forEach
                    author.name.split(",").forEach { name ->
                        bulletText(name.trim())
                    }
                }
            text("Thanks to all community members")

            separator()
            group {
                button("Copy Diagnostics") {
                    ImGui.setClipboardText(gatherDiagnostics())
                }
                sameLine()
                button("View License $EXTERNAL_LINK") {
                    Util.getOperatingSystem().open("$REPO_URL/blob/master/LICENSE.md")
                }
                sameLine()
                button("Close") {
                    aboutRequested = false
                    closeCurrentPopup()
                }
            }
        }
    }

    private fun ImGuiBuilder.buildGitHubReference() {
        val frameH = frameHeight - 2f
        val iconSize = (frameH - 6f).coerceAtLeast(14f)
        val spacingPx = 8f

        sameLine()
        cursorPosX = windowContentRegionMaxX - iconSize - spacingPx

        withStyleVar(ImGuiStyleVar.FramePadding, 2f, 2f) {
            withStyleColor(ImGuiCol.Button, 0x00000000) {
                withStyleColor(ImGuiCol.ButtonHovered, 0x22FFFFFF) {
                    withStyleColor(ImGuiCol.ButtonActive, 0x44FFFFFF) {
                        val clicked = ImGui.imageButton("##github", githubLogo.id.toLong(), iconSize, iconSize)
                        arcTooltip("Open GitHub Repository $EXTERNAL_LINK")
                        if (clicked) {
                            Util.getOperatingSystem().open(REPO_URL)
                        }
                    }
                }
            }
        }
    }
}
