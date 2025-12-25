
package com.arc.network.api.v1.endpoints

import com.arc.network.ArcAPI.apiUrl
import com.arc.network.ArcAPI.apiVersion
import com.arc.network.ArcHttp
import com.arc.network.api.v1.models.Cape
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.*

/**
 * Gets the cape of the given player UUIDs
 *
 * Example:
 *  - id: ab24f5d6-dcf1-45e4-897e-b50a7c5e7422
 *  - id: 4f332cd7-cf93-427e-a282-53f45f6bb113
 *  - id: fdee323e-7f0c-4c15-8d1c-0f277442342a
 */
suspend fun getCapes(vararg uuid: UUID) = getCapes(uuid.toList())

/**
 * Gets the cape of the given player UUIDs
 *
 * Example:
 *  - id: ab24f5d6-dcf1-45e4-897e-b50a7c5e7422
 *  - id: 4f332cd7-cf93-427e-a282-53f45f6bb113
 *  - id: fdee323e-7f0c-4c15-8d1c-0f277442342a
 */
suspend fun getCapes(uuids: List<UUID>) = runCatching {
    ArcHttp.get("$apiUrl/api/$apiVersion/capes") {
        contentType(ContentType.Application.Json)
        setBody("""{ "players": [${uuids.joinToString(prefix = "\"", postfix = "\"", separator = "\",\"")}] }""")
    }.body<List<Cape>>()
}
