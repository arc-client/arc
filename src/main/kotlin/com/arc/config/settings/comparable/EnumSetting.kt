
package com.arc.config.settings.comparable

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.arc.brigadier.CommandResult.Companion.failure
import com.arc.brigadier.CommandResult.Companion.success
import com.arc.brigadier.argument.value
import com.arc.brigadier.argument.word
import com.arc.brigadier.executeWithResult
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.Describable
import com.arc.util.StringUtils.capitalize
import com.arc.util.extension.CommandBuilder
import com.arc.util.extension.displayValue
import net.minecraft.command.CommandRegistryAccess
import kotlin.properties.Delegates

/**
 * @see [com.arc.config.Configurable]
 */
class EnumSetting<T : Enum<T>>(defaultValue: T) : SettingCore<T>(
	defaultValue,
	TypeToken.get(defaultValue.declaringJavaClass).type
) {
    var index by Delegates.observable(value.ordinal) { _, _, to ->
        value = value.enumValues[to % value.enumValues.size]
    }

	context(setting: Setting<*, T>)
    override fun loadFromJson(serialized: JsonElement) {
        super.loadFromJson(serialized)
        index = value.ordinal // super bug fix for imgui
    }

	context(setting: Setting<*, T>)
    override fun ImGuiBuilder.buildLayout() {
        val values = value.enumValues
        val currentDisplay = value.displayValue

        combo("##${setting.name}", preview = "${setting.name}: $currentDisplay") {
            values.forEachIndexed { idx, v ->
                val isSelected = idx == index

                selectable(v.displayValue, isSelected) {
                    if (!isSelected) index = idx
                }

                (v as? Describable)?.let { arcTooltip(it.description) }
            }
        }

        arcTooltip(setting.description)
    }

	context(setting: Setting<*, T>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(word(setting.name)) { parameter ->
            suggests { _, builder ->
                value.enumValues.forEach { builder.suggest(it.name.capitalize()) }
                builder.buildFuture()
            }
            executeWithResult {
                val newValue = value.enumValues.find { it.name.equals(parameter().value(), true) }
                    ?: return@executeWithResult failure("Invalid value")
                setting.trySetValue(newValue)
                return@executeWithResult success()
            }
        }
    }

    companion object {
        val <T : Enum<T>> T.enumValues: Array<T> get() =
            declaringJavaClass.enumConstants
    }
}
