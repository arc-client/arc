
package com.arc.module.modules.movement

import com.arc.event.events.ClientEvent
import com.arc.event.events.MovementEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafe
import com.arc.util.extension.isElytraFlying
import com.arc.util.player.MovementUtils.addSpeed
import net.minecraft.sound.SoundEvents

object ElytraFly : Module(
    name = "ElytraFly",
    description = "Allows you to fly with an elytra",
    tag = ModuleTag.MOVEMENT,
) {
    private val playerBoost by setting("Player Boost", true, description = "Boosts the player when flying")
    private val playerSpeed by setting("Player Speed", 0.02, 0.0..0.5, 0.005, description = "Speed to add when flying") { playerBoost }
    private val rocketBoost by setting("Rocket Boost", false, description = "Boosts the player when using a firework")
    private val rocketSpeed by setting("Rocket Speed", 2.0, 0.0 ..2.0, description = "Speed multiplier that the rocket gives you") { rocketBoost }

    private val mute by setting("Mute Elytra", false, "Mutes the elytra sound when gliding")

    @JvmStatic
    val doBoost: Boolean get() = isEnabled && rocketBoost

    init {
        listen<MovementEvent.Player.Pre> {
            if (playerBoost && player.isElytraFlying && !player.isUsingItem) {
                addSpeed(playerSpeed)
            }
        }

        listen<ClientEvent.Sound> { event ->
            if (!mute) return@listen
            if (event.sound.id != SoundEvents.ITEM_ELYTRA_FLYING.id) return@listen
            event.cancel()
        }
    }

    @JvmStatic
    fun boostRocket() = runSafe {
        val vec = player.rotationVector
        val velocity = player.velocity

        val d = 1.5 * rocketSpeed
        val e = 0.1 * rocketSpeed

        player.velocity = velocity.add(
            vec.x * e + (vec.x * d - velocity.x) * 0.5,
            vec.y * e + (vec.y * d - velocity.y) * 0.5,
            vec.z * e + (vec.z * d - velocity.z) * 0.5
        )
    }
}
