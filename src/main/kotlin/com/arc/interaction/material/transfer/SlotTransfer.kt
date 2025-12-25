
package com.arc.interaction.material.transfer

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.material.StackSelection
import com.arc.interaction.managers.inventory.InventoryRequest.Companion.inventoryRequest
import com.arc.task.Task
import com.arc.util.extension.containerSlots
import com.arc.util.extension.inventorySlots
import com.arc.util.item.ItemUtils.block
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

class SlotTransfer @Ta5kBuilder constructor(
    val screen: ScreenHandler,
    private val selection: StackSelection,
    val from: List<Slot>,
    val to: List<Slot>,
    private val closeScreen: Boolean = true,
    automated: Automated
) : Task<Unit>(), Automated by automated {
    private var selectedFrom = listOf<Slot>()
    private var selectedTo = listOf<Slot>()
    override val name: String
        get() = "Moving $selection from slots [${selectedFrom.joinToString { "${it.id}" }}] to slots [${selectedTo.joinToString { "${it.id}" }}] in ${screen::class.simpleName}"

    private var delay = 0
    private lateinit var changes: InventoryChanges

    override fun SafeContext.onStart() {
        changes = InventoryChanges(player.currentScreenHandler.slots)
    }

    init {
        listen<TickEvent.Pre> {
            if (changes.fulfillsSelection(to, selection)) {
                if (closeScreen) { player.closeHandledScreen() }
                success()
                return@listen
            }

            val current = player.currentScreenHandler
            if (current != screen) {
                failure("Screen has changed. Expected ${screen::class.simpleName} (revision ${screen.revision}) but got ${current::class.simpleName} (revision ${current.revision})")
                return@listen
            }

            selectedFrom = selection.filterSlots(from)
            selectedTo = to.filter { it.stack.isEmpty } + to.filter { it.stack.item.block in inventoryConfig.disposables }

            val nextFrom = selectedFrom.firstOrNull() ?: return@listen
            val nextTo = selectedTo.firstOrNull() ?: return@listen

            inventoryRequest {
                swap(nextTo.id, 1)
                swap(nextFrom.id, 1)
                onComplete { success() }
            }.submit(queueIfMismatchedStage = false)
        }
    }

    companion object {
        @Ta5kBuilder
        fun Automated.moveItems(
            screen: ScreenHandler,
            selection: StackSelection,
            from: List<Slot>,
            to: List<Slot>,
            closeScreen: Boolean = true,
        ) = SlotTransfer(screen, selection, from, to, closeScreen, this)

        @Ta5kBuilder
        context(automated: Automated)
        fun withdraw(screen: ScreenHandler, selection: StackSelection, closeScreen: Boolean = true) =
            automated.moveItems(screen, selection, screen.containerSlots, screen.inventorySlots, closeScreen)

        @Ta5kBuilder
        context(automated: Automated)
        fun deposit(screen: ScreenHandler, selection: StackSelection, closeScreen: Boolean = true) =
            automated.moveItems(screen, selection, screen.inventorySlots, screen.containerSlots, closeScreen)
    }
}
