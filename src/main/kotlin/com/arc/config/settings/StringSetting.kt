
package com.arc.config.settings

import com.google.gson.reflect.TypeToken
import com.arc.brigadier.argument.greedyString
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.config.SettingEditorDsl
import com.arc.config.SettingGroupEditor
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import imgui.flag.ImGuiInputTextFlags
import net.minecraft.command.CommandRegistryAccess

/**
 * @see [com.arc.config.Configurable]
 */
class StringSetting(
    defaultValue: String,
    var multiline: Boolean = false,
    var flags: Int = ImGuiInputTextFlags.None,
) : SettingCore<String>(
	defaultValue,
	TypeToken.get(String::class.java).type
) {
	context(setting: Setting<*, String>)
    override fun ImGuiBuilder.buildLayout() {
        if (multiline) {
            inputTextMultiline(setting.name, ::value, flags = flags)
        } else {
            inputText(setting.name, ::value, flags)
        }
        arcTooltip(setting.description)
    }

	context(setting: Setting<*, String>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(greedyString(setting.name)) { parameter ->
            execute {
                setting.trySetValue(parameter().value())
            }
        }
    }

    companion object {
        @SettingEditorDsl
        @Suppress("unchecked_cast")
        fun SettingGroupEditor.TypedEditBuilder<String>.multiline(multiline: Boolean) {
            (settings as Collection<StringSetting>).forEach { it.multiline = multiline }
        }

        @SettingEditorDsl
        @Suppress("unchecked_cast")
        fun SettingGroupEditor.TypedEditBuilder<String>.flags(flags: Int) {
            (settings as Collection<StringSetting>).forEach { it.flags = flags }
        }
    }
}
