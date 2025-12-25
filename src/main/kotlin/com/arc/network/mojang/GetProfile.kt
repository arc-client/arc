
package com.arc.network.mojang

import com.arc.network.ArcHttp
import com.mojang.authlib.GameProfile
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.util.*

/**
 * Gets a game profile from a username
 *
 * Example:
 *  - name: jeb_
 */
suspend fun getProfile(name: String) = runCatching {
    ArcHttp.get("https://api.mojang.com/users/profiles/minecraft/$name").body<GameProfile>()
}

/**
 * Gets a game profile from a [UUID]
 *
 * Example:
 *  - name: ab24f5d6-dcf1-45e4-897e-b50a7c5e7422
 */
suspend fun getProfile(uuid: UUID) = runCatching {
    ArcHttp.get("https://api.minecraftservices.com/minecraft/profile/lookup/$uuid").body<GameProfile>()
}
