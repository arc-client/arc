
package com.arc.interaction.construction.verify

import com.arc.context.AutomatedSafeContext
import com.arc.context.SafeContext
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.state.property.Property
import net.minecraft.util.math.BlockPos

interface StateMatcher {
    context(safeContext: SafeContext)
    fun matches(
        state: BlockState,
        pos: BlockPos,
        ignoredProperties: Collection<Property<*>> = emptySet()
    ): Boolean

    context(automatedSafeContext: AutomatedSafeContext)
    fun getStack(pos: BlockPos): ItemStack
    context(automatedSafeContext: AutomatedSafeContext)
    fun getState(pos: BlockPos): BlockState
    fun isEmpty(): Boolean
}
