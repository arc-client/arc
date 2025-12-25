
package com.arc.interaction.material.container.containers

import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.util.math.roundedBlockPos
import com.arc.util.text.buildText
import com.arc.util.text.highlighted
import com.arc.util.text.literal
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Box

data class StashContainer(
    val chests: Set<ChestContainer>,
    val pos: Box,
) : MaterialContainer(Rank.Stash) {
    override var stacks: List<ItemStack>
        get() = chests.flatMap { it.stacks }
        set(_) {}

    override val description = buildText {
        literal("Stash at ")
        highlighted(pos.center.roundedBlockPos.toShortString())
    }

    override fun materialAvailable(selection: StackSelection): Int =
        chests.sumOf {
            it.materialAvailable(selection)
        }

    context(safeContext: SafeContext)
    override fun isImmediatelyAccessible() = false
}
