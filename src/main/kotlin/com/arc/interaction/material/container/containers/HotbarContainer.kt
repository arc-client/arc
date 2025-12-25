
package com.arc.interaction.material.container.containers

import com.arc.Arc.mc
import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.ContainerTask
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.interaction.material.transfer.SlotTransfer.Companion.deposit
import com.arc.util.player.SlotUtils.hotbar
import com.arc.util.text.buildText
import com.arc.util.text.literal
import net.minecraft.item.ItemStack

object HotbarContainer : MaterialContainer(Rank.Hotbar) {
    override var stacks: List<ItemStack>
        get() = mc.player?.hotbar ?: emptyList()
        set(_) {}

    override val description = buildText { literal("Hotbar") }

    class HotbarDeposit @Ta5kBuilder constructor(
        val selection: StackSelection,
        automated: Automated
    ) : ContainerTask(), Automated by automated {
        override val name: String get() = "Depositing $selection into hotbar"

        override fun SafeContext.onStart() {
            val handler = player.currentScreenHandler
            deposit(handler, selection).finally {
                delayedFinish()
            }.execute(this@HotbarDeposit)
        }
    }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) = HotbarDeposit(selection, automated)

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = true
}
