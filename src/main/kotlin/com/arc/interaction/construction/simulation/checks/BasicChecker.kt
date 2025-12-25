
package com.arc.interaction.construction.simulation.checks

import com.arc.context.AutomatedSafeContext
import com.arc.interaction.construction.simulation.BreakSimInfo
import com.arc.interaction.construction.simulation.Results
import com.arc.interaction.construction.simulation.SimDsl
import com.arc.interaction.construction.simulation.SimInfo
import com.arc.interaction.construction.simulation.result.results.GenericResult
import com.arc.interaction.construction.simulation.result.results.PreSimResult
import com.arc.util.player.gamemode
import com.arc.util.world.WorldUtils.isLoaded
import net.minecraft.block.OperatorBlock

object BasicChecker : Results<PreSimResult> {
    /**
     * A sequence of basic checks to make sure that the block is worth simulating.
     */
    @SimDsl
    context(automatedSafeContext: AutomatedSafeContext)
    fun SimInfo.hasBasicRequirements(): Boolean = with(automatedSafeContext) {
        // the chunk is not loaded
        if (!isLoaded(pos)) {
            result(PreSimResult.ChunkNotLoaded(pos))
            return false
        }

        // block is already in the correct state
        if (targetState.matches(state, pos)) {
            result(PreSimResult.Done(pos))
            return false
        }

        // block should be ignored
        if (state.block in breakConfig.ignoredBlocks && this@hasBasicRequirements is BreakSimInfo) {
            result(GenericResult.Ignored(pos))
            return false
        }

        // the player is in the wrong game mode to alter the block state
        if (player.isBlockBreakingRestricted(world, pos, gamemode)) {
            result(PreSimResult.Restricted(pos))
            return false
        }

        // the player has no permissions to alter the block state
        if (state.block is OperatorBlock && !player.isCreativeLevelTwoOp) {
            result(PreSimResult.NoPermission(pos, state))
            return false
        }

        // block is outside the world so it cant be altered
        if (!world.worldBorder.contains(pos) || world.isOutOfHeightLimit(pos)) {
            result(PreSimResult.OutOfWorld(pos))
            return false
        }

        // block is unbreakable, so it can't be broken or replaced
        if (state.getHardness(world, pos) < 0 && !gamemode.isCreative) {
            result(PreSimResult.Unbreakable(pos, state))
            return false
        }

        return true
    }
}