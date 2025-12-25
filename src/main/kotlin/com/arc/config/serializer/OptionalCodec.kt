
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import java.lang.reflect.Type
import java.util.*

object OptionalCodec : Codec<Optional<Any>> {
    override fun serialize(src: Optional<Any>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
        src?.map { context?.serialize(it) }?.orElse(JsonNull.INSTANCE) ?: JsonNull.INSTANCE

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): Optional<Any> =
        Optional.ofNullable(json?.let { context?.deserialize(it, typeOfT) ?: Optional.empty<Any>() })
}
