
package com.arc.config.settings

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
import com.arc.util.extension.CommandBuilder
import net.minecraft.command.CommandRegistryAccess

/**
 * @see [com.arc.config.Configurable]
 */
class CharSetting(defaultValue: Char) : SettingCore<Char>(
	defaultValue,
	TypeToken.get(Char::class.java).type
) {
    context(setting: Setting<*, Char>)
	override fun ImGuiBuilder.buildLayout() {}

	context(setting: Setting<*, Char>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(word(setting.name)) { parameter ->
            executeWithResult {
                val char = parameter().value().firstOrNull() ?: return@executeWithResult failure("Cant parse char type")
                setting.trySetValue(char)
                return@executeWithResult success()
            }
        }
    }
}
