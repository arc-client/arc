
package com.arc.mixin.baritone;

import baritone.api.event.events.PlayerUpdateEvent;
import baritone.api.event.events.RotationMoveEvent;
import baritone.api.utils.Rotation;
import baritone.behavior.LookBehavior;
import com.arc.interaction.BaritoneManager;
import com.arc.interaction.managers.rotating.RotationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LookBehavior.class, remap = false)
public class LookBehaviourMixin {
    @Unique
    LookBehavior instance = ((LookBehavior) (Object) this);

    // Redirect baritone's rotations into our rotation engine
    @Inject(method = "updateTarget", at = @At("HEAD"), cancellable = true)
    void onTargetUpdate(Rotation rotation, boolean blockInteract, CallbackInfo ci) {
        if (instance.baritone != BaritoneManager.getPrimary()) return;

        RotationManager.handleBaritoneRotation(rotation.getYaw(), rotation.getPitch());
        ci.cancel();
    }

    @Inject(method = "onPlayerUpdate", at = @At("HEAD"), cancellable = true)
    void onUpdate(PlayerUpdateEvent event, CallbackInfo ci) {
        if (instance.baritone != BaritoneManager.getPrimary()) return;

        ci.cancel();
    }

    @Inject(method = "onPlayerRotationMove", at = @At("HEAD"), cancellable = true)
    void onMovementUpdate(RotationMoveEvent event, CallbackInfo ci) {
        if (instance.baritone != BaritoneManager.getPrimary()) return;

        ci.cancel();
    }
}
