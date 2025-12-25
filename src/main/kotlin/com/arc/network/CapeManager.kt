
package com.arc.network

import com.arc.Arc.LOG
import com.arc.Arc.mc
import com.arc.config.Configurable
import com.arc.config.configurations.SecretsConfig
import com.arc.core.Loadable
import com.arc.event.events.WorldEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.network.api.v1.endpoints.getCape
import com.arc.network.api.v1.endpoints.getCapes
import com.arc.network.api.v1.endpoints.setCape
import com.arc.threading.runGameScheduled
import com.arc.threading.runIO
import com.arc.util.FileUtils.createIfNotExists
import com.arc.util.FileUtils.downloadCompare
import com.arc.util.FileUtils.downloadIfNotPresent
import com.arc.util.FileUtils.ifNotExists
import com.arc.util.FileUtils.isOlderThan
import com.arc.util.FolderRegister.capes
import com.arc.util.StringUtils.asIdentifier
import com.arc.util.extension.resolveFile
import kotlinx.coroutines.runBlocking
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImage.read
import net.minecraft.client.texture.NativeImageBackedTexture
import org.lwjgl.BufferUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object CapeManager : Configurable(SecretsConfig), Loadable {
    override val name: String = "capes"

    var currentCape by setting("cape", "")
        .onValueChangeUnsafe { _, to -> updateCape(to) }

    val cache = ConcurrentHashMap<UUID, String>()
    private val fetchQueue = mutableListOf<UUID>()

    val availableCapes = runBlocking {
        capes.resolveFile("capes.txt")
            .isOlderThan(24.hours) {
                it.downloadIfNotPresent("${ArcAPI.capes}.txt")
                    .onFailure { err -> LOG.error("Could not download the cape list: $err") }
            }
            .ifNotExists {
                it.downloadCompare("${ArcAPI.capes}.txt", -1)
                    .onFailure { err -> LOG.error("Could not download the cape list: $err") }
            }
            .createIfNotExists()
            .readText()
            .split(Regex("\\s+"))
    }

    fun updateCape(cape: String, block: (Throwable?) -> Unit = {}) = runIO {
        setCape(cape).getOrThrow()
        fetchCape(mc.gameProfile.id)
    }.invokeOnCompletion { block(it) }

    fun fetchCape(uuid: UUID, block: (Throwable?) -> Unit = {}) = runIO {
        val cape = getCape(uuid).getOrNull() ?: return@runIO

        val bytes = capes.resolveFile("${cape.id}.png")
            .downloadIfNotPresent(cape.url).getOrNull()
            ?.readBytes() ?: return@runIO

        val buffer = BufferUtils
            .createByteBuffer(bytes.size)
            .put(bytes)
            .flip()

        val image = read(NativeImage.Format.RGBA, buffer)

        runGameScheduled { mc.textureManager.registerTexture(cape.id.asIdentifier, NativeImageBackedTexture({ cape.id }, image)) }

        cache[uuid] = cape.id
    }.invokeOnCompletion { block(it) }

    override fun load() = "Loaded ${availableCapes.size} capes"

    init {
        fixedRateTimer(
            daemon = true,
            name = "Cape-fetcher",
            period = 15.seconds.inWholeMilliseconds,
        ) {
            if (fetchQueue.isEmpty()) return@fixedRateTimer

            runBlocking {
                getCapes(fetchQueue)
                    .onSuccess { it.forEach { cape -> cache[cape.uuid] = cape.id } }

                fetchQueue.clear()
            }
        }

        listen<WorldEvent.Player.Join>(alwaysListen = true) { fetchQueue.add(it.uuid) }
        listen<WorldEvent.Player.Leave>(alwaysListen = true) { fetchQueue.remove(it.uuid) }
    }
}

