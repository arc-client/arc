
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.config.Stringifiable
import com.mojang.serialization.JsonOps
import net.minecraft.item.ItemStack
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrElse

object ItemStackCodec : Codec<ItemStack>, Stringifiable<ItemStack> {
    override fun serialize(
        stack: ItemStack,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement =
        ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack)
            .orThrow

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ItemStack =
        ItemStack.CODEC.parse(JsonOps.INSTANCE, json)
            .result()
            .getOrElse { ItemStack.EMPTY }

    override fun stringify(value: ItemStack) = value.itemName.string.uppercase()
}
