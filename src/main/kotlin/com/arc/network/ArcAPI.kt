
package com.arc.network

import com.arc.Arc.LOG
import com.arc.Arc.mc
import com.arc.config.Configurable
import com.arc.config.configurations.ArcConfig
import com.arc.event.events.ClientEvent
import com.arc.event.events.ConnectionEvent
import com.arc.event.events.ConnectionEvent.Connect.Login.EncryptionResponse
import com.arc.event.listener.UnsafeListener.Companion.listenUnsafe
import com.arc.event.listener.UnsafeListener.Companion.listenConcurrentlyUnsafe
import com.arc.network.NetworkManager.updateToken
import com.arc.network.api.v1.endpoints.login
import com.arc.util.StringUtils.hash
import com.arc.util.extension.isOffline
import net.minecraft.SharedConstants
import net.minecraft.client.network.AllowedAddressResolver
import net.minecraft.client.network.ClientLoginNetworkHandler
import net.minecraft.client.network.ServerAddress
import net.minecraft.network.ClientConnection
import net.minecraft.network.NetworkSide.CLIENTBOUND
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket
import net.minecraft.text.Text
import java.math.BigInteger
import kotlin.jvm.optionals.getOrElse

object ArcAPI : Configurable(ArcConfig) {
    override val name = "api"

    val authServer by setting("Auth Server", "auth.arc-client.org")
    val apiUrl by setting("API Server", "https://api.arc-client.org")
    val apiVersion by setting("API Version", ApiVersion.V1)
    val assets by setting("Assets", "https://raw.githubusercontent.com/Edouard127/arc-assets/refs/heads/master")

    val mappings get() = "$assets/mappings" // Folder containing mappings for our dynamic serializer
    val capes get() = "$assets/capes" // Folder containing all the capes, add .txt to get the list of available capes

    @Suppress("Deprecation")
    const val GAME_VERSION = SharedConstants.VERSION_NAME

    private var hash: String? = null

    init {
        listenConcurrentlyUnsafe<ClientEvent.Startup> { authenticate() }

        listenUnsafe<EncryptionResponse> { event ->
            if (event.secretKey.isDestroyed) return@listenUnsafe

            // Server id is always empty when sent by the Notchian server
            val computed = byteArrayOf()
                .hash("SHA-1", event.secretKey.encoded, event.publicKey.encoded)

            hash = BigInteger(computed).toString(16)
        }

        listenConcurrentlyUnsafe<ConnectionEvent.Connect.Post> {
            // FixMe: If the player have the properties but are invalid this doesn't work
            if (NetworkManager.isValid || mc.gameProfile.isOffline) return@listenConcurrentlyUnsafe

            // If we log in right as the client responds to the encryption request, we start
            // a race condition where the game server haven't acknowledged the packets
            // and posted to the sessionserver api
            login(mc.session.username, hash ?: return@listenConcurrentlyUnsafe)
                .onSuccess { updateToken(it) }
                .onFailure { LOG.warn(it) }
        }
    }

    private fun authenticate() {
        val address = ServerAddress.parse(authServer)
        val connection = ClientConnection(CLIENTBOUND)
        val resolved = AllowedAddressResolver.DEFAULT.resolve(address)
            .map { it.inetSocketAddress }.getOrElse { return }

        ClientConnection.connect(resolved, mc.options.shouldUseNativeTransport(), connection)
            .syncUninterruptibly()

        val handler = ClientLoginNetworkHandler(connection, mc, null, null, false, null, { Text.empty() }, null)

        connection.connect(resolved.hostName, resolved.port, handler)
        connection.send(LoginHelloC2SPacket(mc.session.username, mc.session.uuidOrNull))
    }

    enum class ApiVersion(val value: String) {
        // We can use @Deprecated("Not supported") to remove old API versions in the future
        V1("v1");

        override fun toString() = value
    }
}
