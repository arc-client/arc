
package com.arc.interaction.material.container.containers

import com.arc.Arc.mc
import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.inventory.InventoryRequest.Companion.inventoryRequest
import com.arc.interaction.material.ContainerTask
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.util.item.ItemStackUtils.equal
import com.arc.util.text.buildText
import com.arc.util.text.literal
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

object MainHandContainer : MaterialContainer(Rank.MainHand) {
    override var stacks: List<ItemStack>
        get() = mc.player?.mainHandStack?.let { listOf(it) } ?: emptyList()
        set(_) {}

    override val description = buildText { literal("MainHand") }

    class HandDeposit @Ta5kBuilder constructor(
        val selection: StackSelection,
        val hand: Hand,
        automated: Automated
    ) : ContainerTask(), Automated by automated {
        override val name: String get() = "Depositing [$selection] to ${hand.name.lowercase().replace("_", " ")}"

        init {
            listen<TickEvent.Pre> {
                val moveStack = InventoryContainer.matchingStacks(selection).firstOrNull() ?: run {
                    failure("No matching stacks found in inventory")
                    return@listen
                }

                val handStack = player.getStackInHand(hand)
                if (moveStack.equal(handStack)) {
                    success()
                    return@listen
                }

                inventoryRequest {
                    val stackInOffHand = moveStack.equal(player.offHandStack)
                    val stackInMainHand = moveStack.equal(player.mainHandStack)
                    if ((hand == Hand.MAIN_HAND && stackInOffHand) || (hand == Hand.OFF_HAND && stackInMainHand)) {
                        swapHands()
                        return@inventoryRequest
                    }

                    val slot = player.currentScreenHandler.slots.firstOrNull { it.stack == moveStack }
                        ?: run {
                            failure(IllegalStateException("Cannot find stack in inventory"))
                            return@inventoryRequest
                        }
                    swap(slot.id, player.inventory.selectedSlot)

                    if (hand == Hand.OFF_HAND) swapHands()

                    onComplete { success() }
                }.submit(queueIfMismatchedStage = false)
            }
        }
    }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) = HandDeposit(selection, Hand.MAIN_HAND, automated)

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = true
}
