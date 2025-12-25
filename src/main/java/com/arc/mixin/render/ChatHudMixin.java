
package com.arc.mixin.render;

import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    /**
     * Draws emojis at the given chat position
     * <pre>{@code
     * context.getMatrices().translate(0.0F, 0.0F, 50.0F);
     * context.drawTextWithShadow(this.client.textRenderer, visible.content(), 0, y, 16777215 + (u << 24));
     * context.getMatrices().pop();
     * }</pre>
     */
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
//    int wrapRenderCall(DrawContext instance, TextRenderer textRenderer, OrderedText text, int x, int y, int color, Operation<Integer> original) {
//        return original.call(instance, textRenderer, ArcMoji.INSTANCE.parse(text, x, y, color), 0, y, 16777215 + (color << 24));
//    }
}
