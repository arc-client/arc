
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.config.Stringifiable
import com.arc.util.Formatting.format
import com.mojang.serialization.JsonOps
import net.minecraft.util.math.BlockPos
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrElse

object BlockPosCodec : Codec<BlockPos>, Stringifiable<BlockPos> {
    override fun serialize(
        src: BlockPos,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement =
        BlockPos.CODEC.encodeStart(JsonOps.INSTANCE, src)
            .orThrow

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): BlockPos =
        BlockPos.CODEC.parse(JsonOps.INSTANCE, json)
            .result()
            .getOrElse { BlockPos.ORIGIN }

    override fun stringify(value: BlockPos) = value.format()
}
