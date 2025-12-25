
package com.arc.config.settings.comparable

import com.google.gson.reflect.TypeToken
import com.arc.brigadier.argument.boolean
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import net.minecraft.command.CommandRegistryAccess

/**
 * @see [com.arc.config.Configurable]
 */
class BooleanSetting(defaultValue: Boolean) : SettingCore<Boolean>(
	defaultValue,
	TypeToken.get(Boolean::class.java).type
) {
    context(setting: Setting<*, Boolean>)
	override fun ImGuiBuilder.buildLayout() {
        checkbox(setting.name, ::value)
        arcTooltip(setting.description)
    }

	context(setting: Setting<*, Boolean>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(boolean(setting.name)) { parameter ->
            execute {
                setting.trySetValue(parameter().value())
            }
        }
    }
}
