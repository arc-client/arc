
package com.arc.mixin.render;

import com.arc.module.modules.render.XRay;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockOcclusionCache.class)
public class SodiumBlockOcclusionCacheMixin {
    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private boolean modifyShouldDrawSide(boolean original, BlockState state, BlockView view, BlockPos pos, Direction facing) {
        if (XRay.INSTANCE.isEnabled() && XRay.isSelected(state) && XRay.getOpacity() < 100)
            return true;
        return original;
    }
}
