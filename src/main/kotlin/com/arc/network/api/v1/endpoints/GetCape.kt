
package com.arc.network.api.v1.endpoints

import com.arc.network.ArcAPI.apiUrl
import com.arc.network.ArcAPI.apiVersion
import com.arc.network.ArcHttp
import com.arc.network.api.v1.models.Cape
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.util.*

/**
 * Gets the cape of the given player UUID
 *
 * Example:
 *  - id: ab24f5d6-dcf1-45e4-897e-b50a7c5e7422
 */
suspend fun getCape(uuid: UUID) = runCatching {
    ArcHttp.get("$apiUrl/api/$apiVersion/cape?id=$uuid").body<Cape>()
}
