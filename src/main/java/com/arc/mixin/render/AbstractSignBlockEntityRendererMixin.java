
package com.arc.mixin.render;

import com.arc.module.modules.render.NoRender;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignBlockEntityRenderer.class)
public class AbstractSignBlockEntityRendererMixin {
    @Inject(method = "renderText", at = @At("HEAD"), cancellable = true)
    private void injectRenderText(BlockPos pos, SignText text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int textLineHeight, int maxTextWidth, boolean front, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.getNoSignText()) ci.cancel();
    }
}
