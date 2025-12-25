
package com.arc.config.settings.numeric


import com.arc.brigadier.argument.double
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
class DoubleSetting(
    defaultValue: Double,
    override var range: ClosedRange<Double>,
    override var step: Double,
	unit: String
) : NumericSetting<Double>(
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

	context(setting: Setting<*, Double>)
    override fun ImGuiBuilder.buildSlider() {
        val maxIndex = ((range.endInclusive - range.start) / step).toInt()
        slider("##${setting.name}", ::valueIndex, 0, maxIndex, "")
    }

	context(setting: Setting<*, Double>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(double(setting.name, range.start, range.endInclusive)) { parameter ->
            execute {
                setting.trySetValue(parameter().value())
            }
        }
    }
}
