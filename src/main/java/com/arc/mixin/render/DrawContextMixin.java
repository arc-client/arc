
package com.arc.mixin.render;

import com.arc.module.modules.render.ContainerPreview;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V", at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y, @Nullable Identifier texture, CallbackInfo ci) {
        if (!ContainerPreview.INSTANCE.isEnabled()) return;

        if (ContainerPreview.isRenderingSubTooltip()) return;

        if (ContainerPreview.isLocked()) {
            ci.cancel();
            ContainerPreview.renderLockedTooltip((DrawContext)(Object)this, textRenderer);
            return;
        }

        if (data.isPresent() && data.get() instanceof ContainerPreview.ContainerComponent component) {
            ci.cancel();
            ContainerPreview.renderShulkerTooltip((DrawContext)(Object)this, textRenderer, component, x, y);
        }
    }
}
