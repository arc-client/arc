
package com.arc.interaction.material.container.containers

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.interaction.material.transfer.SlotTransfer.Companion.deposit
import com.arc.interaction.material.transfer.SlotTransfer.Companion.withdraw
import com.arc.task.Task
import com.arc.task.tasks.BuildTask.Companion.breakAndCollectBlock
import com.arc.task.tasks.OpenContainer
import com.arc.task.tasks.PlaceContainer
import com.arc.util.text.buildText
import com.arc.util.text.highlighted
import com.arc.util.text.literal
import net.minecraft.item.ItemStack

data class ShulkerBoxContainer(
    override var stacks: List<ItemStack>,
    val containedIn: MaterialContainer,
    val shulkerStack: ItemStack,
) : MaterialContainer(Rank.ShulkerBox) {
    override val description =
        buildText {
            highlighted(shulkerStack.name.string)
            literal(" in ")
            highlighted(containedIn.name)
            literal(" in slot ")
            highlighted("$slotInContainer")
        }

    private val slotInContainer: Int get() = containedIn.stacks.indexOf(shulkerStack)

    class ShulkerWithdraw(
        private val selection: StackSelection,
        private val shulkerStack: ItemStack,
        automated: Automated
    ) : Task<Unit>(), Automated by automated {
        override val name = "Withdraw $selection from ${shulkerStack.name.string}"

        override fun SafeContext.onStart() {
            PlaceContainer(shulkerStack, this@ShulkerWithdraw).then { placePos ->
                OpenContainer(placePos, this@ShulkerWithdraw).then { screen ->
                    withdraw(screen, selection).then {
                        breakAndCollectBlock(placePos).finally {
                            success()
                        }
                    }
                }
            }.execute(this@ShulkerWithdraw)
        }
    }

    context(automated: Automated)
    override fun withdraw(selection: StackSelection) = ShulkerWithdraw(selection, shulkerStack, automated)

    class ShulkerDeposit(
        private val selection: StackSelection,
        private val shulkerStack: ItemStack,
        automated: Automated
    ) : Task<Unit>(), Automated by automated {
        override val name = "Deposit $selection into ${shulkerStack.name.string}"

        override fun SafeContext.onStart() {
            PlaceContainer(shulkerStack, this@ShulkerDeposit).then { placePos ->
                OpenContainer(placePos, this@ShulkerDeposit).then { screen ->
                    deposit(screen, selection).then {
                        breakAndCollectBlock(placePos).finally {
                            success()
                        }
                    }
                }
            }.execute(this@ShulkerDeposit)
        }
    }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) = ShulkerDeposit(selection, shulkerStack, automated)

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = false
}
