
package com.arc.network.api.v1.endpoints

import com.arc.network.ArcAPI.apiUrl
import com.arc.network.ArcAPI.apiVersion
import com.arc.network.ArcHttp
import com.arc.network.NetworkManager
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Sets the currently authenticated player's cape
 *
 * Example:
 *  - id: galaxy
 */
suspend fun setCape(id: String) = runCatching {
    val resp = ArcHttp.put("$apiUrl/api/$apiVersion/cape?id=$id") {
        bearerAuth(NetworkManager.accessToken)
        contentType(ContentType.Application.Json)
    }

    check(resp.status == HttpStatusCode.OK)
}
