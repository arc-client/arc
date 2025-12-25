
package com.arc.interaction.material.container.containers

import com.arc.Arc.mc
import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.inventory.InventoryRequest.Companion.inventoryRequest
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.task.Task
import com.arc.util.item.ItemStackUtils.equal
import com.arc.util.player.gamemode
import com.arc.util.text.buildText
import com.arc.util.text.literal
import net.minecraft.item.ItemStack

data object CreativeContainer : MaterialContainer(Rank.Creative) {
    override var stacks = emptyList<ItemStack>()

    override val description = buildText { literal("Creative") }

    override fun materialAvailable(selection: StackSelection): Int =
        if (mc.player?.isCreative == true && selection.optimalStack != null) Int.MAX_VALUE else 0

    override fun spaceAvailable(selection: StackSelection): Int =
        if (mc.player?.isCreative == true && selection.optimalStack != null) Int.MAX_VALUE else 0

    class CreativeDeposit @Ta5kBuilder constructor(val selection: StackSelection, automated: Automated) : Task<Unit>(), Automated by automated {
        override val name: String get() = "Removing $selection from creative inventory"

        init {
            listen<TickEvent.Pre> {
                if (!gamemode.isCreative) {
                    // ToDo: Maybe switch gamemode?
                    failure(NotInCreativeModeException())
                }

                inventoryRequest {
                    player.currentScreenHandler?.slots?.let { slots ->
                        selection.filterSlots(slots).forEach {
                            clickCreativeStack(ItemStack.EMPTY, it.id)
                        }
                    }
                    onComplete { success() }
                }.submit(queueIfMismatchedStage = false)
            }
        }
    }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) = CreativeDeposit(selection, automated)

    class CreativeWithdrawal @Ta5kBuilder constructor(val selection: StackSelection, automated: Automated) : Task<Unit>(), Automated by automated {
        override val name: String get() = "Withdrawing $selection from creative inventory"

        init {
            listen<TickEvent.Pre> {
                selection.optimalStack?.let { optimalStack ->
                    if (player.mainHandStack.equal(optimalStack)) {
                        success()
                        return@listen
                    }

                    if (!gamemode.isCreative) {
                        // ToDo: Maybe switch gamemode?
                        failure(NotInCreativeModeException())
                    }

                    inventoryRequest {
                        clickCreativeStack(optimalStack, 36 + player.inventory.selectedSlot)
                        action { player.inventory.selectedStack = optimalStack }
                        onComplete { success() }
                    }.submit(queueIfMismatchedStage = false)
                    return@listen
                }

                failure(IllegalStateException("Cannot move item: no optimal stack"))
            }
        }
    }

    // Withdraws items from the creative menu to the player's main hand
    context(automated: Automated)
    override fun withdraw(selection: StackSelection) = CreativeWithdrawal(selection, automated)

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = safeContext.gamemode.isCreative

    class NotInCreativeModeException : IllegalStateException("Insufficient permission: not in creative mode")
}
