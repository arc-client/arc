
package com.arc.config

import com.arc.config.configurations.AutomationConfigs
import com.arc.config.groups.BreakSettings
import com.arc.config.groups.BuildSettings
import com.arc.config.groups.EatSettings
import com.arc.config.groups.HotbarSettings
import com.arc.config.groups.InteractSettings
import com.arc.config.groups.InventorySettings
import com.arc.config.groups.RotationSettings
import com.arc.context.Automated
import com.arc.event.events.onStaticRender
import com.arc.interaction.construction.simulation.result.Drawable
import com.arc.module.Module
import com.arc.util.NamedEnum


open class AutomationConfig(
    override val name: String,
    configuration: Configuration = AutomationConfigs
) : Configurable(configuration), Automated {
    enum class Group(override val displayName: String) : NamedEnum {
        Build("Build"),
        Break("Break"),
        Interact("Interact"),
        Rotation("Rotation"),
        Inventory("Inventory"),
        Hotbar("Hotbar"),
        Eat("Eat"),
        Render("Render"),
        Debug("Debug")
    }

    override val buildConfig = BuildSettings(this, Group.Build)
    override val breakConfig = BreakSettings(this, Group.Break)
    override val interactConfig = InteractSettings(this, Group.Interact)
    override val rotationConfig = RotationSettings(this, Group.Rotation)
    override val inventoryConfig = InventorySettings(this, Group.Inventory)
    override val hotbarConfig = HotbarSettings(this, Group.Hotbar)
    override val eatConfig = EatSettings(this, Group.Eat)

    companion object {
		context(module: Module)
        fun MutableAutomationConfig.setDefaultAutomationConfig(
	        name: String = module.name,
	        edits: (AutomationConfig.() -> Unit)? = null
		) {
			this.defaultAutomationConfig = AutomationConfig("$name Automation Config").apply { edits?.invoke(this) }
		}

        fun MutableAutomationConfig.setDefaultAutomationConfig(
	        name: String,
	        edits: (AutomationConfig.() -> Unit)? = null
		) {
			defaultAutomationConfig = AutomationConfig("$name Automation Config").apply { edits?.invoke(this) }
		}

        object DEFAULT : AutomationConfig("Default") {
            val renders by setting("Render", false).group(Group.Render)
            val avoidDesync by setting("Avoid Desync", true, "Cancels incoming inventory update packets if they match previous actions").group(Group.Debug)
            val desyncTimeout by setting("Desync Timeout", 30, 1..30, 1, unit = " ticks", description = "Time to store previous inventory actions before dropping the cache") { avoidDesync }.group(Group.Debug)
            val showAllEntries by setting("Show All Entries", false, "Show all entries in the task tree").group(Group.Debug)
            val shrinkFactor by setting("Shrink Factor", 0.001, 0.0..1.0, 0.001).group(Group.Debug)
            val ignoreItemDropWarnings by setting("Ignore Drop Warnings", false, "Hides the item drop warnings from the break manager").group(Group.Debug)
			val managerDebugLogs by setting("Manager Debug Logs", false, "Prints debug logs from managers into chat").group(Group.Debug)

            @Volatile
            var drawables = listOf<Drawable>()

            init {
                onStaticRender {
                    if (renders)
                        with(it) { drawables.forEach { with(it) { buildRenderer() } } }
                }
            }
        }
    }
}