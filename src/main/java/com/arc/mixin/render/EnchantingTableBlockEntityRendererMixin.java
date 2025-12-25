
package com.arc.mixin.render;

import com.arc.module.modules.render.NoRender;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantingTableBlockEntityRenderer.class)
public class EnchantingTableBlockEntityRendererMixin {
    @WrapWithCondition(method = "render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BookModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private boolean wrapRender(BookModel instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j) {
        return NoRender.INSTANCE.isDisabled() || !NoRender.getNoEnchantingTableBook();
    }
}
