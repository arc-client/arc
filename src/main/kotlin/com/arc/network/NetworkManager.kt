
package com.arc.network

import com.arc.Arc.mc
import com.arc.config.Configurable
import com.arc.config.configurations.SecretsConfig
import com.arc.core.Loadable
import com.arc.network.api.v1.models.Authentication
import com.arc.network.api.v1.models.Authentication.Data
import com.arc.util.StringUtils.base64UrlDecode
import com.arc.util.StringUtils.json
import com.arc.util.collections.updatableLazy

object NetworkManager : Configurable(SecretsConfig), Loadable {
    override val name = "network"

    var accessToken by setting("access_token", "") { false }; private set

    val isValid: Boolean
        get() = mc.gameProfile.name == auth.value?.data?.name &&
                mc.gameProfile.id == auth.value?.data?.uuid &&
                System.currentTimeMillis() > (auth.value?.expirationDate ?: Long.MAX_VALUE)

    private val auth = updatableLazy {
        val parts = accessToken.split(".")
        if (parts.size != 3) return@updatableLazy null

        val payload = parts[1]
        val data = payload.base64UrlDecode().json<Data>()

        return@updatableLazy if (System.currentTimeMillis() < data.expirationDate) null
        else data
    }

    fun updateToken(resp: Authentication) {
        accessToken = resp.accessToken
        auth.update()
    }

    override fun load(): String {
        auth.update()

        return auth.value
            ?.let { "Logged you in as ${it.data.name} (${it.data.uuid})" }
            ?: "NetworkManager: You are not authenticated"
    }
}
