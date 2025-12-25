
package com.arc.interaction.construction.simulation

import baritone.api.pathing.goals.Goal
import com.arc.util.world.fastVectorOf
import com.arc.util.world.toFastVec
import net.minecraft.util.math.BlockPos

class BuildGoal(
    val sim: Simulation,
    blocked: BlockPos
) : Goal {
    private val blockedVec = blocked.toFastVec()

    override fun isInGoal(x: Int, y: Int, z: Int): Boolean {
        val pos = fastVectorOf(x, y, z)
        return sim.simulate(pos).any { it.rank.ordinal < 4 } && blockedVec != pos
    }

    override fun heuristic(x: Int, y: Int, z: Int): Double {
        val bestRank = sim.simulate(fastVectorOf(x, y, z))
            .minOrNull()?.rank?.ordinal ?: 100000
        return 1 / (bestRank.toDouble() + 1)
    }
}
