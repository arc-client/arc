
package com.arc.module.modules.combat

import com.arc.context.SafeContext
import com.arc.event.events.ConnectionEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.event.listener.SafeListener.Companion.listenConcurrently
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.network.mojang.getProfile
import com.arc.threading.onShutdown
import com.arc.util.Timer
import com.arc.util.player.spawnFakePlayer
import com.mojang.authlib.GameProfile
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.client.network.PlayerListEntry
import java.util.*
import kotlin.time.Duration.Companion.seconds

object FakePlayer : Module(
    name = "FakePlayer",
    description = "Spawns a fake player",
    tag = ModuleTag.COMBAT,
) {
    private val playerName by setting("Name", "Steve")

    private var fakePlayer_field: OtherClientPlayerEntity? = null
    private var SafeContext.fakePlayer
        get() = fakePlayer_field
        set(value) { fakePlayer_field = value; value?.let { world.addEntity(it) } }

    private val nilProfile: GameProfile
        get() = GameProfile(UUID(0, 0), playerName)

    private val cachedProfiles = mutableMapOf<String, GameProfile>()
    private val fetchTimer = Timer()

    init {
        listen<TickEvent.Pre> {
            fakePlayer = cachedProfiles[playerName]
                ?.let { spawnFakePlayer(it, fakePlayer ?: player, addToWorld = false) }
                ?.takeUnless { it == fakePlayer?.gameProfile }
                ?: fakePlayer?.takeIf { playerName == it.gameProfile.name }
                        ?: spawnFakePlayer(nilProfile, fakePlayer ?: player, addToWorld = false)
        }

        listenConcurrently<TickEvent.Pre>(priority = 1000) {
            if (!fetchTimer.timePassed(2.seconds)) return@listenConcurrently

            cachedProfiles.getOrPut(playerName) { fetchProfile(playerName) }
        }

        listen<ConnectionEvent.Connect.Pre> { disable() }
        onShutdown { disable() }
        onDisable { fakePlayer?.discard(); fakePlayer = null }
    }

    suspend fun SafeContext.fetchProfile(user: String): GameProfile {
        val requestedProfile = getProfile(user).getOrElse { return nilProfile }

        // Fetch the skin properties from mojang
        val properties = mc.sessionService.fetchProfile(requestedProfile.id, true)?.profile?.properties

        // We use the nil profile to avoid the nil username if something wrong happens
        // Check the GameProfile deserializer you'll understand
        val profile = nilProfile
        properties?.let { profile.properties.putAll(it) }

        mc.networkHandler?.playerListEntries?.put(profile.id, PlayerListEntry(profile, false))

        return profile
    }
}
