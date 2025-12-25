
package com.arc.util

import com.arc.util.math.MathUtils.floorToInt
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos

object EntityUtils {
    fun Entity.getPositionsWithinHitboxXZ(minY: Int, maxY: Int): Set<BlockPos> {
        val hitbox = boundingBox
        val minX = hitbox.minX.floorToInt()
        val maxX = hitbox.maxX.floorToInt()
        val minZ = hitbox.minZ.floorToInt()
        val maxZ = hitbox.maxZ.floorToInt()
        val positions = mutableSetOf<BlockPos>()
        (minX..maxX).forEach { x ->
            (minY..maxY).forEach { y ->
                (minZ..maxZ).forEach { z ->
                    positions.add(BlockPos(x, y, z))
                }
            }
        }
        return positions
    }
}