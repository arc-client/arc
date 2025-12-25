
package com.arc.config.settings.collections

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.arc.Arc.gson
import com.arc.config.Setting
import com.arc.config.serializer.BlockCodec
import com.arc.gui.dsl.ImGuiBuilder
import net.minecraft.block.Block

class BlockCollectionSetting(
	immutableCollection: Collection<Block>,
	defaultValue: MutableCollection<Block>,
) : CollectionSetting<Block>(
	defaultValue,
	immutableCollection,
	TypeToken.getParameterized(Collection::class.java, Block::class.java).type
) {
	context(setting: Setting<*, MutableCollection<Block>>)
	override fun ImGuiBuilder.buildLayout() = buildComboBox("block") { BlockCodec.stringify(it) }

	context(setting: Setting<*, MutableCollection<Block>>)
	override fun toJson(): JsonElement = gson.toJsonTree(value, type)

	context(setting: Setting<*, MutableCollection<Block>>)
	override fun loadFromJson(serialized: JsonElement) {
		value = gson.fromJson<Collection<Block>>(serialized, type)
			.toMutableList()
	}
}