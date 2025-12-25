
package com.arc.mixin.baritone;

import baritone.Baritone;
import baritone.api.utils.Rotation;
import baritone.utils.player.BaritonePlayerContext;
import com.arc.interaction.BaritoneManager;
import com.arc.interaction.managers.rotating.RotationManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BaritonePlayerContext.class, remap = false) // fix compileJava warning
public class BaritonePlayerContextMixin {
    @Shadow
    @Final
    private Baritone baritone;

    // Let baritone know the actual rotation
    @Inject(method = "playerRotations", at = @At("HEAD"), cancellable = true, remap = false)
    void syncRotationWithBaritone(CallbackInfoReturnable<Rotation> cir) {
        if (baritone != BaritoneManager.getPrimary()) return;

        RotationManager rm = RotationManager.INSTANCE;
        cir.setReturnValue(new Rotation(
                (float) rm.getActiveRotation().getYaw(), (float) rm.getActiveRotation().getPitch())
        );
    }
}
