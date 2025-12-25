
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.config.Stringifiable
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.lang.reflect.Type

object ItemCodec : Codec<Item>, Stringifiable<Item> {
	override fun serialize(
		item: Item,
		typeOfSrc: Type,
		context: JsonSerializationContext
	): JsonElement = JsonPrimitive(item.toString())

	override fun deserialize(
		json: JsonElement,
		typeOfT: Type,
		context: JsonDeserializationContext
	): Item =
		Registries.ITEM.get(Identifier.of(json.asString)) // Watch out!! Errors are silently catched by gson!!

	override fun stringify(value: Item) = value.name.string.replaceFirstChar { it.uppercase() }
}