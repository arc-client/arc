
package com.arc.util.extension

import com.arc.context.SafeContext
import com.arc.util.world.FastVector
import com.arc.util.world.toBlockPos
import com.arc.util.world.x
import com.arc.util.world.y
import com.arc.util.world.z
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.World
import java.awt.Color

val SafeContext.worldName: String
    get() = when {
        mc.currentServerEntry != null -> "Multiplayer"; mc.isIntegratedServerRunning -> "Singleplayer"; else -> "Main Menu"
    }
val World?.isOverworld: Boolean get() = this?.registryKey == World.OVERWORLD
val World?.isNether: Boolean get() = this?.registryKey == World.NETHER
val World?.isEnd: Boolean get() = this?.registryKey == World.END
val World?.dimensionName: String
    get() = when {
        isOverworld -> "Overworld"
        isNether -> "Nether"
        isEnd -> "End"
        else -> "Unknown"
    }

fun SafeContext.collisionShape(state: BlockState, pos: BlockPos): VoxelShape =
    state.getCollisionShape(world, pos).offset(pos)

fun SafeContext.outlineShape(state: BlockState, pos: BlockPos) =
    state.getOutlineShape(world, pos).offset(pos)

fun SafeContext.blockColor(state: BlockState, pos: BlockPos) =
    Color(state.getMapColor(world, pos).color)

fun World.getBlockState(x: Int, y: Int, z: Int): BlockState {
    if (isOutOfHeightLimit(y)) return Blocks.VOID_AIR.defaultState

    val chunk = getChunk(x shr 4, z shr 4)
    val sectionIndex = getSectionIndex(y)
    if (sectionIndex < 0) return Blocks.VOID_AIR.defaultState

    val section = chunk.getSection(sectionIndex)
    return section.getBlockState(x and 0xF, y and 0xF, z and 0xF)
}

fun World.getFluidState(x: Int, y: Int, z: Int): FluidState {
    if (isOutOfHeightLimit(y)) return Fluids.EMPTY.defaultState

    val chunk = getChunk(x shr 4, z shr 4)
    val sectionIndex = getSectionIndex(y)
    if (sectionIndex < 0) return Fluids.EMPTY.defaultState

    val section = chunk.getSection(sectionIndex)
    return section.getFluidState(x and 0xF, y and 0xF, z and 0xF)
}

fun World.getBlockState(vec: FastVector): BlockState = getBlockState(vec.x, vec.y, vec.z)
fun World.getBlockEntity(vec: FastVector) = getBlockEntity(vec.toBlockPos())
fun World.getFluidState(vec: FastVector): FluidState = getFluidState(vec.x, vec.y, vec.z)
