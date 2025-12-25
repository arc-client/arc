
package com.arc.config.settings.collections

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.arc.Arc.gson
import com.arc.config.Setting
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.reflections.className

/**
 * @see [com.arc.config.settings.collections.CollectionSetting]
 * @see [com.arc.config.Configurable]
 */
class ClassCollectionSetting<T : Any>(
	private val immutableCollection: Collection<T>,
	defaultValue: MutableCollection<T>
) : CollectionSetting<T>(
	defaultValue,
	immutableCollection,
	TypeToken.getParameterized(Collection::class.java, Any::class.java).type
) {
	context(setting: Setting<*, MutableCollection<T>>)
	override fun ImGuiBuilder.buildLayout() = buildComboBox("item") { it.className }

	// When serializing the list to json we do not want to serialize the elements' classes, but their stringified representation.
	// If we do serialize the classes we'll run into missing type adapters errors by Gson.
	// This is intended behaviour. If you wish your collection settings to display something else then you must extend this class.
	context(setting: Setting<*, MutableCollection<T>>)
	override fun toJson(): JsonElement = gson.toJsonTree(value.map { it.className })

	context(setting: Setting<*, MutableCollection<T>>)
	override fun loadFromJson(serialized: JsonElement) {
		val strList = gson.fromJson<MutableList<String>>(serialized, type)
			.mapNotNull { str -> immutableCollection.find { it.className == str } }
			.toMutableList()

		value = strList
	}
}