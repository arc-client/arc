
package com.arc.interaction.material.container.containers

import com.arc.Arc.mc
import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.util.text.buildText
import com.arc.util.text.literal
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

object OffHandContainer : MaterialContainer(Rank.OffHand) {
    override var stacks: List<ItemStack>
        get() = mc.player?.offHandStack?.let { listOf(it) } ?: emptyList()
        set(_) {}

    override val description = buildText { literal("OffHand") }

    context(automated: Automated)
    override fun deposit(selection: StackSelection) = MainHandContainer.HandDeposit(selection, Hand.OFF_HAND, automated)

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = true
}
