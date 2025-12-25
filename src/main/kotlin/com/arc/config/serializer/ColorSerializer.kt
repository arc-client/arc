
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.config.Stringifiable
import java.awt.Color
import java.lang.reflect.Type

object ColorSerializer : Codec<Color>, Stringifiable<Color> {
    override fun serialize(
        src: Color,
        typeOfSrc: Type,
        context: JsonSerializationContext?,
    ): JsonElement =
        JsonPrimitive("${src.red},${src.green},${src.blue},${src.alpha}")

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext?,
    ): Color =
        json.asString.split(",").let {
            when (it.size) {
                3 -> Color(it[0].toInt(), it[1].toInt(), it[2].toInt())
                4 -> Color(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt())
                else -> throw JsonParseException("Invalid color format")
            }
        }

    override fun stringify(value: Color) = "${value.red},${value.green},${value.blue},${value.alpha}"
}
