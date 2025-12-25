
package com.arc.config.settings

import com.google.gson.reflect.TypeToken
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.config.SettingEditorDsl
import com.arc.config.SettingGroupEditor
import com.arc.gui.dsl.ImGuiBuilder
import imgui.ImGui
import imgui.ImGui.calcTextSize
import imgui.ImGui.dummy
import imgui.flag.ImGuiCol
import java.text.NumberFormat
import java.util.*

/**
 * @see [com.arc.config.Configurable]
 */
abstract class NumericSetting<T>(
	defaultValue: T,
	open var range: ClosedRange<T>,
	open var step: T,
	var unit: String
) : SettingCore<T>(
	defaultValue,
	TypeToken.get(defaultValue::class.java).type
) where T : Number, T : Comparable<T> {
	override var value: T
		get() = super.value
		set(newVal) {
			super.value = newVal.coerceIn(range)
		}

    private val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

    override fun toString() = "${formatter.format(value)}$unit"

    /**
     * Subclasses must implement this to provide their specific slider widget.
     */
    context(setting: Setting<*, T>)
    protected abstract fun ImGuiBuilder.buildSlider()

	context(setting: Setting<*, T>)
    override fun ImGuiBuilder.buildLayout() {
        val showReset = setting.isModified
        val resetButtonText = "R"
        val valueString = this@NumericSetting.toString()

        buildSlider()
        arcTooltip(setting.description)

        val itemRectMin = ImGui.getItemRectMin()
        val itemRectMax = ImGui.getItemRectMax()
        val textHeight = ImGui.getTextLineHeight()
        val textY = itemRectMin.y + (itemRectMax.y - itemRectMin.y - textHeight) / 2.0f
        val labelWidth = calcTextSize(setting.name).x
        val valueWidth = calcTextSize(valueString).x

        val labelEndPosX = itemRectMin.x + style.framePadding.x * 2 + labelWidth
        val valueStartPosX = itemRectMax.x - style.framePadding.x * 2 - valueWidth

        windowDrawList.addText(itemRectMin.x + style.framePadding.x * 2, textY, ImGui.getColorU32(ImGuiCol.Text), setting.name)
        if (labelEndPosX < valueStartPosX) {
            windowDrawList.addText(valueStartPosX, textY, ImGui.getColorU32(ImGuiCol.Text), valueString)
        }

        sameLine(0.0f, style.itemSpacing.x)
        if (showReset) {
            button("$resetButtonText##${setting.name}") {
                setting.reset()
            }
            onItemHover {
                tooltip { text("Reset to default") }
            }
        } else {
            dummy(calcTextSize(resetButtonText).x + style.framePadding.x * 2.0f, ImGui.getFrameHeight())
        }
    }

    companion object {
        @SettingEditorDsl
        @Suppress("unchecked_cast")
        fun <T> SettingGroupEditor.TypedEditBuilder<T>.range(range: ClosedRange<T>) where T : Number, T : Comparable<T> {
            (settings as Collection<NumericSetting<T>>).forEach { it.range = range }
        }

        @SettingEditorDsl
        @Suppress("unchecked_cast")
        fun <T> SettingGroupEditor.TypedEditBuilder<T>.step(step: T) where T : Number, T : Comparable<T> {
            (settings as Collection<NumericSetting<T>>).forEach { it.step = step }
        }

        @SettingEditorDsl
        @Suppress("unchecked_cast")
        fun <T> SettingGroupEditor.TypedEditBuilder<T>.unit(unit: String) where T : Number, T : Comparable<T> {
            (settings as Collection<NumericSetting<T>>).forEach { it.unit = unit}
        }
    }
}
