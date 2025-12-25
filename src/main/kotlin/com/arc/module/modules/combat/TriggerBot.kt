
package com.arc.module.modules.combat

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.math.random
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult

object TriggerBot : Module(
    name = "TriggerBot",
    description = "Automatically attacks entities when looking at them",
    tag = ModuleTag.COMBAT,
) {
    private val delay by setting("Delay", 50, 0..500, 10, "Delay in ms between attacks")
    private val randomDelay by setting("Random Delay", 20, 0..200, 5, "Random variation added to delay")
    private val cooldownCheck by setting("Cooldown Check", true, "Only attack when attack cooldown is ready")
    private val swing by setting("Swing", true, "Swing hand when attacking")

    private var lastAttackTime = 0L

    init {
        listen<TickEvent.Pre> {
            val hitResult = mc.crosshairTarget ?: return@listen
            if (hitResult !is EntityHitResult) return@listen

            val entity = hitResult.entity
            if (entity !is LivingEntity || entity == player) return@listen
            if (!entity.isAlive) return@listen

            // Cooldown check
            if (cooldownCheck && player.getAttackCooldownProgress(0.5f) < 1.0f) return@listen

            // Delay check
            val currentTime = System.currentTimeMillis()
            val totalDelay = delay + (0..randomDelay).random()
            if (currentTime - lastAttackTime < totalDelay) return@listen

            // Attack
            interaction.attackEntity(player, entity)
            if (swing) player.swingHand(Hand.MAIN_HAND)

            lastAttackTime = currentTime
        }

        onDisable { lastAttackTime = 0L }
    }
}
