
package com.arc.config.settings.numeric

import com.arc.brigadier.argument.integer
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.settings.NumericSetting
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import net.minecraft.command.CommandRegistryAccess

/**
 * @see [com.arc.config.Configurable]
 */
class IntegerSetting(
    defaultValue: Int,
    override var range: ClosedRange<Int>,
    override var step: Int = 1,
    unit: String
) : NumericSetting<Int>(
    defaultValue,
    range,
    step,
    unit
) {
	context(setting: Setting<*, Int>)
    override fun ImGuiBuilder.buildSlider() {
        slider("##${setting.name}", ::value, range.start, range.endInclusive, "")
    }

	context(setting: Setting<*, Int>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(integer(setting.name, range.start, range.endInclusive)) { parameter ->
            execute {
                setting.trySetValue(parameter().value())
            }
        }
    }
}
