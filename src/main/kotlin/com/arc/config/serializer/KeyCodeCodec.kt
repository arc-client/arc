
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.util.KeyCode
import java.lang.reflect.Type

object KeyCodeCodec : Codec<KeyCode> {
    override fun serialize(
        src: KeyCode?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement =
        src?.let {
            JsonPrimitive(it.name)
        } ?: JsonNull.INSTANCE

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): KeyCode =
        json?.asString?.let(KeyCode::fromKeyName) ?: throw JsonParseException("Invalid key code format")
}
