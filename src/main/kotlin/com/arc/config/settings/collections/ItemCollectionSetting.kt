
package com.arc.config.settings.collections

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.arc.Arc.gson
import com.arc.config.Setting
import com.arc.config.serializer.ItemCodec
import com.arc.gui.dsl.ImGuiBuilder
import net.minecraft.item.Item

class ItemCollectionSetting(
	immutableCollection: Collection<Item>,
	defaultValue: MutableCollection<Item>
) : CollectionSetting<Item>(
	defaultValue,
	immutableCollection,
	TypeToken.getParameterized(Collection::class.java, Item::class.java).type
) {
	context(setting: Setting<*, MutableCollection<Item>>)
	override fun ImGuiBuilder.buildLayout() = buildComboBox("item") { ItemCodec.stringify(it) }

	context(setting: Setting<*, MutableCollection<Item>>)
	override fun toJson(): JsonElement = gson.toJsonTree(value, type)

	context(setting: Setting<*, MutableCollection<Item>>)
	override fun loadFromJson(serialized: JsonElement) {
		value = gson.fromJson<Collection<Item>>(serialized, type)
			.toMutableList()
	}
}