
package com.arc.interaction.material.container

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.ContainerManager.findContainerWithMaterial
import com.arc.interaction.material.container.containers.ShulkerBoxContainer
import com.arc.interaction.material.transfer.TransferResult
import com.arc.task.Task
import com.arc.util.Communication.logError
import com.arc.util.Nameable
import com.arc.util.item.ItemStackUtils.count
import com.arc.util.item.ItemStackUtils.empty
import com.arc.util.item.ItemStackUtils.shulkerBoxContents
import com.arc.util.item.ItemStackUtils.spaceLeft
import com.arc.util.item.ItemUtils
import com.arc.util.item.ItemUtils.toItemCount
import com.arc.util.text.TextBuilder
import com.arc.util.text.TextDsl
import com.arc.util.text.buildText
import com.arc.util.text.highlighted
import com.arc.util.text.literal
import com.arc.util.text.text
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

// ToDo: Make jsonable to persistently store them
abstract class MaterialContainer(
    val rank: Rank
) : Nameable, Comparable<MaterialContainer> {
    abstract var stacks: List<ItemStack>
    abstract val description: Text

    @TextDsl
    fun TextBuilder.stock(selection: StackSelection) {
        literal("\n")
        literal("Contains ")
        val available = materialAvailable(selection)
        highlighted(if (available == Int.MAX_VALUE) "∞" else available.toItemCount())
        literal(" of ")
        highlighted("${selection.optimalStack?.name?.string}")
        literal("\n")
        literal("Could store ")
        val left = spaceAvailable(selection)
        highlighted(if (left == Int.MAX_VALUE) "∞" else left.toItemCount())
        literal(" of ")
        highlighted("${selection.optimalStack?.name?.string}")
    }

    fun description(selection: StackSelection) =
        buildText {
            text(description)
            stock(selection)
        }

    override val name: String
        get() = buildText { text(description) }.string

    val shulkerContainer
        get() =
            stacks.filter {
                it.item in ItemUtils.shulkerBoxes
            }.map { stack ->
                ShulkerBoxContainer(
                    stack.shulkerBoxContents,
                    containedIn = this@MaterialContainer,
                    shulkerStack = stack
                )
            }.toSet()

    fun update(stacks: List<ItemStack>) {
        this.stacks = stacks
    }

    class FailureTask(override val name: String) : Task<Unit>() {
        override fun SafeContext.onStart() {
            failure(name)
        }
    }

    class AwaitItemTask(
        override val name: String,
        val selection: StackSelection,
        automated: Automated
    ) : Task<Unit>(), Automated by automated {
        init {
            listen<TickEvent.Post> {
                if (selection.findContainerWithMaterial() != null) {
                    success()
                }
            }
        }

        override fun SafeContext.onStart() {
            logError(name)
        }
    }

    /**
     * Withdraws items from the container to the player's inventory.
     */
    @Task.Ta5kBuilder
    context(automated: Automated)
    open fun withdraw(selection: StackSelection): Task<*>? = null

    /**
     * Deposits items from the player's inventory into the container.
     */
    @Task.Ta5kBuilder
    context(automated: Automated)
    open fun deposit(selection: StackSelection): Task<*>? = null

    open fun matchingStacks(selection: StackSelection) =
        selection.filterStacks(stacks)

    open fun materialAvailable(selection: StackSelection) =
        matchingStacks(selection).count

    open fun spaceAvailable(selection: StackSelection) =
        matchingStacks(selection).spaceLeft + stacks.empty * selection.stackSize

    context(safeContext: SafeContext)
    abstract fun isImmediatelyAccessible(): Boolean

    context(automated: Automated)
    fun transfer(selection: StackSelection, destination: MaterialContainer): TransferResult {
        val amount = materialAvailable(selection)
        if (amount < selection.count) {
            return TransferResult.MissingItems(selection.count - amount)
        }

//        val space = destination.spaceAvailable(selection)
//        if (space == 0) {
//            return TransferResult.NoSpace
//        }

//        val transferAmount = minOf(amount, space)
//        selection.selector = { true }
//        selection.count = transferAmount

        return TransferResult.ContainerTransfer(selection, from = this, to = destination, automated)
    }

    enum class Rank {
        MainHand,
        OffHand,
        Hotbar,
        Inventory,
        Creative,
        ShulkerBox,
        EnderChest,
        Chest,
        Stash
    }

    override fun compareTo(other: MaterialContainer) =
        compareBy<MaterialContainer> {
            it.rank
        }.compare(this, other)
}
