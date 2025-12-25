
package com.arc.mixin.entity;

import com.arc.Arc;
import com.arc.event.EventFlow;
import com.arc.event.events.MovementEvent;
import com.arc.interaction.managers.rotating.RotationManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "clipAtLedge", at = @At(value = "HEAD"), cancellable = true)
    private void injectSafeWalk(CallbackInfoReturnable<Boolean> cir) {
        MovementEvent.ClipAtLedge event = new MovementEvent.ClipAtLedge(((PlayerEntity) (Object) this).isSneaking());
        cir.setReturnValue(EventFlow.post(event).getClip());
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float wrapHeadYaw(PlayerEntity instance, Operation<Float> original) {
        if ((Object) this != Arc.getMc().player) {
            return original.call(instance);
        }

        Float yaw = RotationManager.getHeadYaw();
        return (yaw != null) ? yaw : original.call(instance);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float wrapAttackYaw(PlayerEntity instance, Operation<Float> original) {
        if ((Object) this != Arc.getMc().player) {
            return original.call(instance);
        }

        Float yaw = RotationManager.getMovementYaw();
        return (yaw != null) ? yaw : original.call(instance);
    }
}
