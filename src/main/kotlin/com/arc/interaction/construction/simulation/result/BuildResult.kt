
package com.arc.interaction.construction.simulation.result

import com.arc.util.Nameable
import net.minecraft.util.math.BlockPos

abstract class BuildResult : Nameable, ComparableResult<Rank> {
    abstract val pos: BlockPos
    override val compareBy = this

    final override fun compareTo(other: ComparableResult<Rank>) = super.compareTo(other)
}