
package com.arc.mixin.render;

import com.arc.Arc;
import com.arc.interaction.managers.rotating.RotationManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.arc.util.math.LinearKt.lerp;

// This mixin's purpose is to set the player's pitch the current render pitch to correctly show the rotation
// regardless of the camera position
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    /**
     * Uses the current rotation render pitch
     * <pre>{@code
     * float g = MathHelper.lerpAngleDegrees(f, livingEntity.lastHeadYaw, livingEntity.headYaw);
     * livingEntityRenderState.bodyYaw = clampBodyYaw(livingEntity, g, f);
     * livingEntityRenderState.relativeHeadYaw = MathHelper.wrapDegrees(g - livingEntityRenderState.bodyYaw);
     * livingEntityRenderState.pitch = livingEntity.getLerpedPitch(f);
     * livingEntityRenderState.customName = livingEntity.getCustomName();
     * livingEntityRenderState.flipUpsideDown = shouldFlipUpsideDown(livingEntity);
     *     if (livingEntityRenderState.flipUpsideDown) {
     *     livingEntityRenderState.pitch *= -1.0F;
     *     livingEntityRenderState.relativeHeadYaw *= -1.0F;
     * }
     * }</pre>
     */
    @WrapOperation(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F"))
    private float wrapGetLerpedPitch(LivingEntity livingEntity, float v, Operation<Float> original) {
        Float headPitch = RotationManager.getHeadPitch();
        if (livingEntity != Arc.getMc().player || headPitch == null) {
            return original.call(livingEntity, v);
        }

        return lerp(v, RotationManager.getPrevServerRotation().getPitchF(), headPitch);
    }
}
