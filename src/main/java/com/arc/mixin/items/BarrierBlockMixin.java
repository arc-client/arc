
package com.arc.mixin.items;

import com.arc.module.modules.render.BlockESP;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BarrierBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BarrierBlock.class)
public class BarrierBlockMixin {
    /**
     * Modifies barrier block render type to {@link BlockRenderType#MODEL} when {@link BlockESP} is enabled and {@link BlockESP#getBarrier()} is true
     */
    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private BlockRenderType modifyGetRenderType(BlockRenderType original, BlockState state) {
        if (BlockESP.INSTANCE.isEnabled()
                && BlockESP.getBarrier()
                && state.getBlock() == Blocks.BARRIER
        ) return BlockRenderType.MODEL;
        return original;
    }
}
