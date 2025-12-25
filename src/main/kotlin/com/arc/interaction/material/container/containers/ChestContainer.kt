
package com.arc.interaction.material.container.containers

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.interaction.material.transfer.SlotTransfer.Companion.deposit
import com.arc.interaction.material.transfer.SlotTransfer.Companion.withdraw
import com.arc.task.tasks.OpenContainer
import com.arc.util.Communication.info
import com.arc.util.text.buildText
import com.arc.util.text.highlighted
import com.arc.util.text.literal
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

data class ChestContainer(
    override var stacks: List<ItemStack>,
    val blockPos: BlockPos,
    val containedInStash: StashContainer? = null
) : MaterialContainer(Rank.Chest) {
    override val description =
        buildText {
            literal("Chest at ")
            highlighted(blockPos.toShortString())
            containedInStash?.let { stash ->
                literal(" (contained in ")
                highlighted(stash.name)
                literal(")")
            }
        }

//    override fun prepare() =
//        moveIntoEntityRange(blockPos).onSuccess { _, _ ->
////            when {
////                ChestBlock.hasBlockOnTop(world, blockPos) -> breakBlock(blockPos.up())
////                ChestBlock.hasCatOnTop(world, blockPos) -> kill(cat)
////            }
//            if (ChestBlock.isChestBlocked(world, blockPos)) {
//                throw ChestBlockedException()
//            }
//        }

    context(automated: Automated)
    override fun withdraw(selection: StackSelection) =
        OpenContainer(blockPos, automated)
            .then {
                info("Withdrawing $selection from ${it.type}")
                withdraw(it, selection)
            }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) =
        OpenContainer(blockPos, automated)
            .then {
                info("Depositing $selection to ${it.type}")
                deposit(it, selection)
            }

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = false
}
