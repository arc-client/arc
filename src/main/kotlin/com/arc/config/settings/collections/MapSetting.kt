
package com.arc.config.settings.collections

import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import java.lang.reflect.Type

/**
 * @see [com.arc.config.Configurable]
 */
class MapSetting<K, V>(
	defaultValue: MutableMap<K, V>,
	type: Type
) : SettingCore<MutableMap<K, V>>(
	defaultValue,
	type
) {
    context(setting: Setting<*, MutableMap<K, V>>)
	override fun ImGuiBuilder.buildLayout() {}
}
