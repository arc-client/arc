
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.config.Stringifiable
import com.mojang.serialization.JsonOps
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import java.lang.reflect.Type

object BlockCodec : Codec<Block>, Stringifiable<Block> {
    override fun serialize(
        src: Block,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement =
        Registries.BLOCK.codec.encodeStart(JsonOps.INSTANCE, src)
            .orThrow

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): Block =
        Registries.BLOCK.codec.parse(JsonOps.INSTANCE, json)
            .orThrow

    override fun stringify(value: Block) = Registries.BLOCK.getId(value).path.replaceFirstChar { it.uppercase() }
}
