
package com.arc.mixin.render;

import com.arc.module.modules.render.NoRender;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeadFeatureRenderer.class)
public class HeadFeatureRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void injectRender(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntityRenderState livingEntityRenderState, float f, float g, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.getNoArmor() && NoRender.getIncludeNoOtherHeadItems()) ci.cancel();
    }
}
