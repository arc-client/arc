
package com.arc.config.settings.complex

import com.google.gson.reflect.TypeToken
import com.arc.brigadier.argument.integer
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.optional
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import net.minecraft.command.CommandRegistryAccess
import java.awt.Color

/**
 * @see [com.arc.config.Configurable]
 */
class ColorSetting(defaultValue: Color) : SettingCore<Color>(
	defaultValue,
	TypeToken.get(Color::class.java).type
) {
    context(setting: Setting<*, Color>)
	override fun ImGuiBuilder.buildLayout() {
        colorEdit(setting.name, ::value)
        arcTooltip(setting.description)
    }

	context(setting: Setting<*, Color>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(integer("Red", 0, 255)) { red ->
            required(integer("Green", 0, 255)) { green ->
                required(integer("Blue", 0, 255)) { blue ->
                    optional(integer("Alpha", 0, 255)) { alpha ->
                        execute {
                            val alphaValue = alpha?.let { it().value() } ?: 255
                            setting.trySetValue(Color(red().value(), green().value(), blue().value(), alphaValue))
                        }
                    }
                }
            }
        }
    }
}
