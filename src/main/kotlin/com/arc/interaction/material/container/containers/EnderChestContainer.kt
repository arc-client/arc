
package com.arc.interaction.material.container.containers

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.task.Task
import com.arc.util.Communication.info
import com.arc.util.text.buildText
import com.arc.util.text.literal
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

object EnderChestContainer : MaterialContainer(Rank.EnderChest) {
    override var stacks = emptyList<ItemStack>()
    private var placePos: BlockPos? = null

    override val description = buildText { literal("Ender Chest") }

//    override fun prepare(): Task<*> {
//        TODO("Not yet implemented")
//    }
//        findBlock(Blocks.ENDER_CHEST).onSuccess { pos ->
//            moveIntoEntityRange(pos)
//            placePos = pos
//        }.onFailure {
//            acquireStack(Items.ENDER_CHEST.select()).onSuccess { _, stack ->
//                placeContainer(stack).onSuccess { _, pos ->
//                    placePos = pos
//                }
//            }
//        }

    class EnderchestWithdrawal @Ta5kBuilder constructor(selection: StackSelection) : Task<Unit>() {
        override val name = "Withdrawing $selection from ender chest"

        override fun SafeContext.onStart() {
            info("Not yet implemented")
            success()
        }
    }

    context(automated: Automated)
    override fun withdraw(selection: StackSelection) = EnderchestWithdrawal(selection)

    class EnderchestDeposit @Ta5kBuilder constructor(selection: StackSelection) : Task<Unit>() {
        override val name = "Depositing $selection into ender chest"

        override fun SafeContext.onStart() {
            info("Not yet implemented")
            success()
        }
    }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) = EnderchestDeposit(selection)

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = false
}
