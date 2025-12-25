
package com.arc.config.settings.numeric

import com.arc.brigadier.argument.float
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.settings.NumericSetting
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import com.arc.util.math.MathUtils.roundToStep
import net.minecraft.command.CommandRegistryAccess
import kotlin.math.roundToInt

/**
 * @see [com.arc.config.Configurable]
 */
class FloatSetting(
    defaultValue: Float,
    override var range: ClosedRange<Float>,
    override var step: Float = 1f,
    unit: String,
) : NumericSetting<Float>(
    defaultValue,
    range,
    step,
    unit
) {
    private var valueIndex: Int
        get() = ((value - range.start) / step).roundToInt()
        set(index) {
            value = (range.start + index * step)
	            .roundToStep(step)
	            .coerceIn(range)
        }

	context(setting: Setting<*, Float>)
    override fun ImGuiBuilder.buildSlider() {
        val maxIndex = ((range.endInclusive - range.start) / step).toInt()
        slider("##${setting.name}", ::valueIndex, 0, maxIndex, "")
    }

	context(setting: Setting<*, Float>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(float(setting.name, range.start, range.endInclusive)) { parameter ->
            execute {
                setting.trySetValue(parameter().value())
            }
        }
    }
}
