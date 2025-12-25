
package com.arc.interaction.material.container.containers

import com.arc.Arc.mc
import com.arc.context.SafeContext
import com.arc.interaction.material.container.MaterialContainer
import com.arc.util.player.SlotUtils.combined
import com.arc.util.text.buildText
import com.arc.util.text.literal
import net.minecraft.item.ItemStack

object InventoryContainer : MaterialContainer(Rank.Inventory) {
    override var stacks: List<ItemStack>
        get() = mc.player?.combined ?: emptyList()
        set(_) {}

    override val description = buildText { literal("Inventory") }

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = true
}
