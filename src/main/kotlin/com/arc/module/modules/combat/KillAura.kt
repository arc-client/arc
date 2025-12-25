
package com.arc.module.modules.combat

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.config.groups.Targeting
import com.arc.event.events.PlayerPacketEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.rotating.RotationRequest
import com.arc.interaction.managers.rotating.visibilty.lookAtEntity
import com.arc.interaction.material.StackSelection.Companion.selectStack
import com.arc.interaction.material.container.ContainerManager.transfer
import com.arc.interaction.material.container.containers.MainHandContainer
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.threading.runSafeAutomated
import com.arc.util.NamedEnum
import com.arc.util.item.ItemStackUtils.attackDamage
import com.arc.util.item.ItemStackUtils.equal
import com.arc.util.math.random
import com.arc.util.player.SlotUtils.hotbarAndStorage
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Hand

object KillAura : Module(
    name = "KillAura",
    description = "Attacks entities",
    tag = ModuleTag.COMBAT,
) {
    // Interact
    private val rotate by setting("Rotate", true).group(Group.General)
    private val swap by setting("Swap", true, "Swap to the item with the highest damage").group(Group.General)
    private val attackMode by setting("Attack Mode", AttackMode.Cooldown).group(Group.General)
    private val cooldownShrink by setting("Cooldown Offset", 0, 0..5, 1) { attackMode == AttackMode.Cooldown }.group(Group.General)
    private val hitDelay1 by setting("Hit Delay 1", 2.0, 0.0..20.0, 1.0) { attackMode == AttackMode.Delay }.group(Group.General)
    private val hitDelay2 by setting("Hit Delay 2", 6.0, 0.0..20.0, 1.0) { attackMode == AttackMode.Delay }.group(Group.General)

    // Targeting
    private val targeting = Targeting.Combat(this, Group.Targeting)

    val target: LivingEntity?
        get() = targeting.target()

    private var lastAttackTime = 0L
    private var hitDelay = 100.0

    private var prevY = 0.0
    private var lastY = 0.0
    private var lastOnGround = true

    enum class Group(override val displayName: String) : NamedEnum {
        General("General"),
        Targeting("Targeting"),
    }

    enum class AttackMode {
        Cooldown,
        Delay
    }

    init {
        setDefaultAutomationConfig {
            applyEdits {
                hideAllGroupsExcept(buildConfig)
                buildConfig.apply {
                    hide(::pathing, ::stayInRange, ::collectDrops, ::spleefEntities, ::maxPendingActions, ::actionTimeout, ::maxBuildDependencies, ::blockReach)
                }
            }
        }

        listen<PlayerPacketEvent.Pre>(Int.MIN_VALUE) { event ->
            prevY = lastY
            lastY = event.position.y
            lastOnGround = event.onGround
        }

        listen<TickEvent.Pre> {
            target?.let { entity ->
                if (swap) {
                    val selection = selectStack().sortByDescending { player.attackDamage(stack = it) }

                    if (!selection.bestItemMatch(player.hotbarAndStorage).equal(player.mainHandStack))
                        selection.transfer(MainHandContainer)?.run()
                }

                // Wait until the rotation has a hit result on the entity
                if (rotate) runSafeAutomated {
                    val rotationRequest = RotationRequest(lookAtEntity(entity)?.rotation ?: return@listen, this@KillAura).submit()
                    if (!rotationRequest.done) return@listen
                }

                // Cooldown check
                when (attackMode) {
                    AttackMode.Cooldown -> if (player.getAttackCooldownProgress(0.5f) + (cooldownShrink / 20f) < 1.0f) return@listen
                    AttackMode.Delay -> if (System.currentTimeMillis() - lastAttackTime < hitDelay) return@listen
                }

                // Attack
                interaction.attackEntity(player, target)
                if (interactConfig.swing) player.swingHand(Hand.MAIN_HAND)

                lastAttackTime = System.currentTimeMillis()
                hitDelay = (hitDelay1..hitDelay2).random() * 50
            }
        }

        onEnable { reset() }
        onDisable { reset() }
    }

    private fun reset() {
        lastY = 0.0
        prevY = 0.0
        lastOnGround = true

        lastAttackTime = 0L
        hitDelay = 100.0
    }
}
