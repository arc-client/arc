
package com.arc.config.settings.numeric

import com.arc.brigadier.argument.long
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
class LongSetting(
    defaultValue: Long,
    override var range: ClosedRange<Long>,
    override var step: Long = 1,
    unit: String
) : NumericSetting<Long>(
    defaultValue,
    range,
    step,
    unit
) {
    // ToDo: No worky for super large numbers
    private var valueIndex: Int
        get() = ((value - range.start) / step).toInt()
        set(index) {
            value = (range.start + index * step).coerceIn(range)
        }

	context(setting: Setting<*, Long>)
    override fun ImGuiBuilder.buildSlider() {
        val maxIndex = ((range.endInclusive - range.start) / step).toInt()
        slider("##${setting.name}", ::valueIndex, 0, maxIndex, "")
    }

	context(setting: Setting<*, Long>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(long(setting.name, range.start, range.endInclusive)) { parameter ->
            execute {
                setting.trySetValue(parameter().value())
            }
        }
    }
}
