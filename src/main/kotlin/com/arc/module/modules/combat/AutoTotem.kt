
package com.arc.module.modules.combat

import com.arc.config.groups.InventorySettings
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.friend.FriendManager
import com.arc.interaction.managers.inventory.InventoryRequest.Companion.inventoryRequest
import com.arc.interaction.material.StackSelection.Companion.select
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.NamedEnum
import com.arc.util.combat.CombatUtils.hasDeadlyCrystal
import com.arc.util.combat.DamageUtils.isFallDeadly
import com.arc.util.extension.fullHealth
import com.arc.util.extension.tickDelta
import com.arc.util.world.fastEntitySearch
import net.minecraft.entity.mob.CreeperEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items

object AutoTotem : Module(
    name = "AutoTotem",
    description = "Swaps the your off-hand item to a totem",
    tag = ModuleTag.COMBAT,
) {
    private val log by setting("Log Message", true).group(Group.General)
	private val always by setting("Always", true, "Always attempt to keep a totem in offhand").group(Group.General)
	private val ignoreWhenHolding by setting("Ignore When Holding", false, "Ignore swapping to offhand when already holding a totem").group(Group.General)
	private val minimumHealth by setting("Min Health", 10, 6..36, 1, "Set the minimum health threshold to swap", unit = " half-hearts") { !always }.group(Group.General)
    private val falls by setting("Falls", true, "Swap if the player will die of fall damage") { !always }.group(Group.General)
    private val fallDistance by setting("Falls Time", 10, 0..30, 1, "Number of blocks fallen before swapping", unit = " blocks") { !always && falls }.group(Group.General)
    private val crystals by setting("Crystals", true, "Swap if an End Crystal explosion would be lethal") { !always }.group(Group.General)
    private val creeper by setting("Creepers", true, "Swap when an ignited Creeper is nearby") { !always }.group(Group.General)
    private val players by setting("Players", false, "Swap if a nearby player is detected within the set distance") { !always }.group(Group.General)
    private val minPlayerDistance by setting("Player Distance", 64, 32..128, 4, "Set the distance to detect players to swap") { !always && players }.group(Group.General)
    private val friends by setting("Friends", false, "Exclude friends from triggering player-based swaps") { !always && players }.group(Group.General)

    override val inventoryConfig = InventorySettings(this, Group.Inventory)

    init {
        listen<TickEvent.Pre> {
            if (!always && Reason.entries.none { it.check(this) }) return@listen

            if ((!ignoreWhenHolding || !player.isHolding(Items.TOTEM_OF_UNDYING)) && player.offHandStack.item != Items.TOTEM_OF_UNDYING) {
                Items.TOTEM_OF_UNDYING.select()
	                .filterSlots(player.currentScreenHandler.slots)
	                .takeIf { it.isNotEmpty() }
	                ?.let { totems ->
		                val cursor = player.currentScreenHandler.cursorStack
		                val targetSlot = player.currentScreenHandler.slots
			                .findLast { !cursor.isEmpty && it.canInsert(cursor) }

						inventoryRequest {
							targetSlot?.let { pickup(it.id, 0) }
							swap(totems.first().id, 40)
						}.submit()
	                }
            }
        }
    }

    enum class Reason(val check: SafeContext.() -> Boolean) {
        Health({ player.fullHealth < minimumHealth }),
        Creeper({ creeper && fastEntitySearch<CreeperEntity>(15.0).any {
            it.getLerpedFuseTime(mc.tickDelta) > 0.0
                    && it.pos.distanceTo(player.pos) <= 5.0
        } }),
        Player({ players && fastEntitySearch<PlayerEntity>(minPlayerDistance.toDouble()).any { otherPlayer ->
            otherPlayer != player
                    && player.distanceTo(otherPlayer) <= minPlayerDistance
                    && (!friends || !FriendManager.isFriend(otherPlayer.uuid))
        } }),
        EndCrystal({ crystals && hasDeadlyCrystal() }),
        FallDamage({ falls && isFallDeadly() && player.fallDistance > fallDistance })
    }

    enum class Group(override val displayName: String): NamedEnum {
        General("General"),
        Inventory("Inventory"),
    }
}
