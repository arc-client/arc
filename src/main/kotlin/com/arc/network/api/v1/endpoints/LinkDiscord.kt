
package com.arc.network.api.v1.endpoints

import com.arc.network.ArcAPI.apiUrl
import com.arc.network.ArcAPI.apiVersion
import com.arc.network.ArcHttp
import com.arc.network.NetworkManager
import com.arc.network.api.v1.models.Authentication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Links a Discord account to a session account
 *
 * Example:
 *  - token: OTk1MTU1NzcyMzYxMTQ2NDM4
 */
suspend fun linkDiscord(discordToken: String) = runCatching {
    ArcHttp.post("${apiUrl}/api/$apiVersion/link/discord") {
        setBody("""{ "token": "$discordToken" }""")
        bearerAuth(NetworkManager.accessToken)
        contentType(ContentType.Application.Json)
    }.body<Authentication>()
}
