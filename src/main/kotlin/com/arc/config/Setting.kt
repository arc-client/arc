
package com.arc.config

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.arc.Arc.LOG
import com.arc.Arc.gson
import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.string
import com.arc.brigadier.argument.value
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.command.CommandRegistry
import com.arc.command.commands.ConfigCommand
import com.arc.config.Setting.ValueListener
import com.arc.context.SafeContext
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.threading.runSafe
import com.arc.util.Communication.info
import com.arc.util.Describable
import com.arc.util.Nameable
import com.arc.util.NamedEnum
import com.arc.util.extension.CommandBuilder
import com.arc.util.text.ClickEvents
import com.arc.util.text.HoverEvents
import com.arc.util.text.TextBuilder
import com.arc.util.text.buildText
import com.arc.util.text.clickEvent
import com.arc.util.text.highlighted
import com.arc.util.text.hoverEvent
import com.arc.util.text.literal
import net.minecraft.command.CommandRegistryAccess
import java.lang.reflect.Type
import kotlin.reflect.KProperty

/**
 * Represents a setting with a [defaultValue], [visibility] condition, and [description].
 * This setting is serializable ([Jsonable]) and has a [name].
 *
 * When the [value] is modified, all registered [listeners] are notified.
 * The [visibility] of the setting can be checked with the [isVisible] property.
 * The setting can be [reset] to its [defaultValue].
 *
 * Simple Usage:
 * ```kotlin
 * // this uses the delegate (by) association to access the setting value in the code directly.
 * val mode by setting("Mode", Modes.FREEZE, { page == Page.CUSTOM }, "The mode of the module.")
 *
 * init {
 *     listener<TickEvent.Pre> {
 *         LOG.info("Mode: $mode") // direct access of the value
 *     }
 * }
 * ```
 *
 * Advanced usage with listeners:
 * ```kotlin
 * // notice how this does not use the delegate (by) association, to access the setting object to register listeners.
 * val mode = setting("Mode", Modes.FREEZE, { page == Page.CUSTOM }, "The mode of the module.")
 *
 * init {
 *     mode.listener { from, to ->
 *        // Do something when the mode changes in a safe context
 *     }
 *     mode.unsafeListener { from, to ->
 *        // Do something when the mode changes in an unsafe context
 *     }
 *
 *     listener<TickEvent.Pre> {
 *         LOG.info("Mode: ${mode.value}") // indirect access of the value
 *     }
 * }
 * ```
 *
 * @property defaultValue The default value of the setting.
 * @property description A description of the setting.
 * @property type The type reflection of the setting.
 * @property visibility A function that determines whether the setting is visible.
 */
abstract class SettingCore<T : Any>(
	var defaultValue: T,
	val type: Type
) {
    open var value = defaultValue
	    set(value) {
		    val oldValue = field
		    field = value
		    listeners.forEach {
			    if (it.requiresValueChange && oldValue == value) return@forEach
			    it.execute(oldValue, value)
		    }
		}
	val listeners = mutableListOf<ValueListener<T>>()

	context(setting: Setting<*, T>)
	abstract fun ImGuiBuilder.buildLayout()

	context(setting: Setting<*, T>)
	open fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
		required(string("value as JSON")) { value ->
			executeWithResult {
				val valueString = value().value()
				val parsed = try {
					JsonParser.parseString("\"$valueString\"")
				} catch (_: Exception) {
					return@executeWithResult failure("$valueString is not a valid JSON string.")
				}
					?: return@executeWithResult failure("No config found for $name.")
				val previous = this@SettingCore.value
				try {
					loadFromJson(parsed)
				} catch (_: Exception) {
					return@executeWithResult failure("Failed to load $valueString as a ${type::class.simpleName} for $name in ${setting.configurable.name}.")
				}
				ConfigCommand.info(setting.setMessage(previous, this@SettingCore.value))
				return@executeWithResult success()
			}
		}
	}

	context(setting: Setting<*, T>)
	open fun toJson(): JsonElement =
		gson.toJsonTree(value, type)

	context(setting: Setting<*, T>)
	open fun loadFromJson(serialized: JsonElement) {
		runCatching {
			value = gson.fromJson(serialized, type)
		}.onFailure {
			LOG.warn("Failed to load setting ${setting.name} with value $serialized. Resetting to default value $defaultValue")
			value = defaultValue
		}
	}
}

class Setting<T : SettingCore<R>, R : Any>(
	override val name: String,
	override val description: String,
	var core: T,
	val configurable: Configurable,
	val visibility: () -> Boolean,
) : Nameable, Describable {
	val originalCore = core
	var disabled = { false }
	var groups: MutableList<List<NamedEnum>> = mutableListOf()

	var value by this

	val isModified get() = value != core.defaultValue

	operator fun getValue(thisRef: Any?, property: KProperty<*>) = core.value
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
		core.value = value
	}

	fun reset(silent: Boolean = false) {
		if (!silent && value == core.defaultValue) {
			ConfigCommand.info(notChangedMessage())
			return
		}
		if (!silent) ConfigCommand.info(resetMessage(value, core.defaultValue))
		value = core.defaultValue
	}

	fun restoreOriginalCore() {
		core = originalCore
	}

	fun ImGuiBuilder.buildLayout() = with(core) { buildLayout() }
	fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) = with(core) { buildCommand(registry) }

	fun toJson() = originalCore.toJson()
	fun loadFromJson(serialized: JsonElement) = originalCore.loadFromJson(serialized)

	class ValueListener<T>(val requiresValueChange: Boolean, val execute: (from: T, to: T) -> Unit)

	/**
	 * Will only register changes of the variable, not the content of the variable!
	 * E.g., if the variable is a list, it will only register if the list reference changes, not if the content of the list changes.
	 */
	fun onValueChange(block: SafeContext.(from: R, to: R) -> Unit) = apply {
		core.listeners.add(ValueListener(true) { from, to ->
			runSafe {
				block(from, to)
			}
		})
	}

	fun onValueChangeUnsafe(block: (from: R, to: R) -> Unit) = apply {
		core.listeners.add(ValueListener(true, block))
	}

	fun onValueSet(block: (from: R, to: R) -> Unit) = apply {
		core.listeners.add(ValueListener(false, block))
	}

	fun disabled(predicate: () -> Boolean) = apply {
		disabled = predicate
	}

	fun group(path: List<NamedEnum>, vararg continuation: NamedEnum) = apply {
		groups.add(path + continuation)
	}

	fun group(vararg path: NamedEnum) = apply {
		groups.add(path.toList())
	}

	fun group(path: NamedEnum?) = apply {
		path?.let { groups.add(listOf(it)) }
	}

	fun trySetValue(newValue: R) {
		if (newValue == value) {
			ConfigCommand.info(notChangedMessage())
		} else {
			val previous = value
			value = newValue
			ConfigCommand.info(setMessage(previous, newValue))
		}
	}

	fun setMessage(previousValue: R, newValue: R) = buildText {
		literal("Set ")
		changedMessage(previousValue, newValue)
		clickEvent(ClickEvents.suggestCommand("${CommandRegistry.prefix}${ConfigCommand.name} reset ${configurable.commandName} $commandName")) {
			hoverEvent(HoverEvents.showText(buildText {
				literal("Click to reset to default value ")
				highlighted(core.defaultValue.toString())
			})) {
				highlighted(" [Reset]")
			}
		}
	}

	private fun resetMessage(previousValue: R, newValue: R) = buildText {
		literal("Reset ")
		changedMessage(previousValue, newValue)
	}

	private fun notChangedMessage() = buildText {
		literal("No changes made to ")
		highlighted(name)
		literal(" as it is already set to ")
		highlighted(value.toString())
		literal(".")
	}

	private fun TextBuilder.changedMessage(previousValue: R, newValue: R) {
		highlighted(configurable.name)
		literal(" > ")
		highlighted(name)
		literal(" from ")
		highlighted(previousValue.toString())
		literal(" to ")
		highlighted(newValue.toString())
		literal(".")
		clickEvent(ClickEvents.suggestCommand("${CommandRegistry.prefix}${ConfigCommand.name} set ${configurable.commandName} $commandName $previousValue")) {
			hoverEvent(HoverEvents.showText(buildText {
				literal("Click to undo to previous value ")
				highlighted(previousValue.toString())
			})) {
				highlighted(" [Undo]")
			}
		}
	}

	override fun toString() = "Setting $name: $value of type ${core.type.typeName}"

	override fun equals(other: Any?) = other is Setting<*, *> && name == other.name
	override fun hashCode() = name.hashCode()
}