
package com.arc.module.modules.render

import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks

object XRay : Module(
    name = "XRay",
    description = "Allows you to see ores through walls",
    tag = ModuleTag.RENDER,
) {
    val defaultBlocks = setOf(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS
    )

    @JvmStatic
    val opacity by setting("Opacity", 40, 0..100, 1, "Opacity of the non x-rayed blocks, (automatically overridden as 0 when running Sodium)")
        .onValueChange { _, _ -> if (isEnabled) mc.worldRenderer.reload() }

    private val selection by setting("Block Selection", defaultBlocks, description = "Block selection that will be shown (whitelist) or hidden (blacklist)")
        .onValueChange { _, _ -> if (isEnabled) mc.worldRenderer.reload() }
    private val mode by setting("Selection Mode", Selection.Whitelist, "The mode of the block selection")

    @JvmStatic
    fun isSelected(blockState: BlockState) = mode.select(blockState)

    enum class Selection(val select: (BlockState) -> Boolean) {
        Whitelist({ it.block in selection }),
        Blacklist({ it.block !in selection })
    }

    init {
        onToggle {
            mc.worldRenderer.reload()
        }
    }
}
