
package com.arc.mixin.render;

import com.arc.module.modules.render.ContainerPreview;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ContainerPreview.INSTANCE.isEnabled() && ContainerPreview.isLocked()) {
            if (ContainerPreview.isMouseOverLockedTooltip((int) mouseX, (int) mouseY)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ContainerPreview.INSTANCE.isEnabled() && ContainerPreview.isLocked()) {
            if (ContainerPreview.isMouseOverLockedTooltip((int) mouseX, (int) mouseY)) {
                cir.setReturnValue(true);
            }
        }
    }
}
