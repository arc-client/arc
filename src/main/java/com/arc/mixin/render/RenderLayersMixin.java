
package com.arc.mixin.render;

import com.arc.module.modules.render.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class RenderLayersMixin {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void injectGetBlockLayer(BlockState state, CallbackInfoReturnable<RenderLayer> cir) {
        if (XRay.INSTANCE.isDisabled()) return;
        final var opacity = XRay.getOpacity();
        if (opacity <= 0 || opacity >= 100) return;
        if (!XRay.isSelected(state)) cir.setReturnValue(RenderLayer.getTranslucent());
    }

    @Inject(method = "getFluidLayer", at = @At("HEAD"), cancellable = true)
    private static void injectGetFluidLayer(FluidState state, CallbackInfoReturnable<RenderLayer> cir) {
        if (XRay.INSTANCE.isDisabled()) return;
        final var opacity = XRay.getOpacity();
        if (opacity > 0 && opacity < 100) cir.setReturnValue(RenderLayer.getTranslucent());
    }
}
