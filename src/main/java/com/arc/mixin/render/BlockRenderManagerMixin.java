
package com.arc.mixin.render;

import com.arc.module.modules.render.BlockESP;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderManager.class)
public abstract class BlockRenderManagerMixin {
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void getModel(BlockState state, CallbackInfoReturnable<BlockStateModel> cir) {
        if (BlockESP.INSTANCE.isEnabled()
                && BlockESP.getBarrier()
                && state.getBlock() == Blocks.BARRIER
        ) cir.setReturnValue(BlockESP.getModel());
    }
}
