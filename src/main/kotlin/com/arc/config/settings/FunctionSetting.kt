
package com.arc.config.settings

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.reflect.TypeToken
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder

open class FunctionSetting<T>(defaultValue: () -> T) : SettingCore<() -> T>(
	defaultValue,
	TypeToken.get(defaultValue::class.java).type
) {
    context(setting: Setting<*, () -> T>)
	override fun ImGuiBuilder.buildLayout() {
        button(setting.name) { value() }
        arcTooltip(setting.description)
    }

	context(setting: Setting<*, () -> T>)
    override fun toJson(): JsonElement = JsonNull.INSTANCE
	context(setting: Setting<*, () -> T>)
    override fun loadFromJson(serialized: JsonElement) { value = defaultValue }
}
