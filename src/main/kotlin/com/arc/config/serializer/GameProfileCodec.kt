
package com.arc.config.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.arc.config.Codec
import com.arc.config.Stringifiable
import com.mojang.authlib.GameProfile
import java.lang.reflect.Type
import java.util.*

// Yeah yeah I know, there's already a serializer for GameProfile in the Minecraft codebase.
// But who cares, I'm doing it again.
// What you gon' do bout it, huh?
// That's what I thought.
object GameProfileCodec : Codec<GameProfile>, Stringifiable<GameProfile> {
    override fun serialize(
        src: GameProfile,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement =
        JsonObject().apply {
            addProperty("name", src.name)
            addProperty("id", src.id.toString())
        }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): GameProfile {
        val name = json.asJsonObject.get("name")?.asString ?: "nil"
        val id = json.asJsonObject.get("id")?.asString ?: "00000000-0000-0000-0000-000000000000"
        val parsedId =
            if (id.length == 32) id.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(),
                "$1-$2-$3-$4-$5"
            )
            else id

        return GameProfile(UUID.fromString(parsedId), name)
    }

    override fun stringify(value: GameProfile) = value.toString()
}
