
package com.arc.network.api.v1.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Player(
    @SerializedName("name")
    val name: String,

    @SerializedName("id")
    val uuid: UUID,

    @SerializedName("discord_id")
    val discordId: String,

    // Whether the player is verified or not
    @SerializedName("unsafe")
    val unsafe: Boolean,
)
