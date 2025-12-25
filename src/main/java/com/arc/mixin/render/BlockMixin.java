
package com.arc.mixin.render;

import com.arc.module.modules.render.XRay;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public class BlockMixin {
    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private static boolean modifyShouldDrawSide(boolean original, BlockState state, BlockState otherState, Direction side) {
        if (XRay.INSTANCE.isEnabled() && XRay.isSelected(state) && XRay.getOpacity() < 100)
            return true;
        return original;
    }
}
