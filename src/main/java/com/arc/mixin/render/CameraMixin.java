
package com.arc.mixin.render;

import com.arc.interaction.managers.rotating.RotationManager;
import com.arc.module.modules.player.Freecam;

import com.arc.module.modules.render.FreeLook;
import com.arc.module.modules.render.NoRender;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    public abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(
            BlockView area,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (!Freecam.INSTANCE.isEnabled()) return;

        Freecam.updateCam();
    }

    /**
     * Sets the lock rotation to the active rotation
     * <pre>{@code
     * this.setPos(
     *     MathHelper.lerp((double)tickDelta, focusedEntity.prevX, focusedEntity.getX()),
     *     MathHelper.lerp((double)tickDelta, focusedEntity.prevY, focusedEntity.getY()) + (double)MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY),
     *     MathHelper.lerp((double)tickDelta, focusedEntity.prevZ, focusedEntity.getZ())
     *     );
     * }</pre>
     */
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    private void injectQuickPerspectiveSwap(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var rot = RotationManager.getLockRotation();
        if (rot == null) return;
        setRotation(rot.getYawF(), rot.getPitchF());
    }

    /**
     * Allows camera to clip through blocks in third person
     */
    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> info) {
        if (FreeLook.INSTANCE.isEnabled()) {
            info.setReturnValue(desiredCameraDistance);
        }
    }

    /**
     * Modifies the third person camera distance
     * <pre>{@code
     * if (thirdPerson) {
     *         if (inverseView) {
     *             this.setRotation(this.yaw + 180.0F, -this.pitch);
     *         }
     *
     *         this.moveBy(-this.clipToSpace(4.0), 0.0, 0.0);
     * }
     * }</pre>
     */
    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
    private float onDistanceUpdate(float desiredCameraDistance) {
        if (FreeLook.INSTANCE.isEnabled()) {
            return 4.0F;
        }

        return desiredCameraDistance;
    }

    /**
     * Modifies the arguments for setting the camera rotation.
     * Mixes into 4 arguments:
     * <p>Experimental Minecart Controller:</p>
     * <pre>
     * if (experimentalMinecartController.hasCurrentLerpSteps()) {
     *     Vec3d vec3d = minecartEntity.getPassengerRidingPos(focusedEntity).subtract(minecartEntity.getPos()).subtract(focusedEntity.getVehicleAttachmentPos(minecartEntity)).add(new Vec3d(0.0, (double)MathHelper.lerp(tickProgress, this.lastCameraY, this.cameraY), 0.0));
     *     this.setRotation(focusedEntity.getYaw(tickProgress), focusedEntity.getPitch(tickProgress));
     *     this.setPos(experimentalMinecartController.getLerpedPosition(tickProgress).add(vec3d));
     *     break label39;
     * }
     * </pre>
     * <p>Default Camera:</p>
     * <pre>
     * this.setRotation(focusedEntity.getYaw(tickProgress), focusedEntity.getPitch(tickProgress));
     * this.setPos(MathHelper.lerp((double)tickProgress, focusedEntity.lastX, focusedEntity.getX()), MathHelper.lerp((double)tickProgress, focusedEntity.lastY, focusedEntity.getY()) + (double)MathHelper.lerp(tickProgress, this.lastCameraY, this.cameraY), MathHelper.lerp((double)tickProgress, focusedEntity.lastZ, focusedEntity.getZ()));
     * </pre>
     * <p>Third person camera:</p>
     * <pre>
     * if (thirdPerson) {
     *     if (inverseView) {
     *         this.setRotation(this.yaw + 180.0F, -this.pitch);
     *     }
     *     // ...
     * }
     * </pre>
     * <p>When the player is focused on another Living Entity:</p>
     * <pre>
     * Direction direction = ((LivingEntity)focusedEntity).getSleepingDirection();
     * this.setRotation(direction != null ? direction.getPositiveHorizontalDegrees() - 180.0F : 0.0F, 0.0F);
     * this.moveBy(0.0F, 0.3F, 0.0F);
     * </pre>
     */
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        if (FreeLook.INSTANCE.isEnabled()) {
            args.set(0, FreeLook.INSTANCE.getCamera().getYawF());
            args.set(1, FreeLook.INSTANCE.getCamera().getPitchF());
        }
    }

    @Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
    private void injectGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.getNoFluidOverlay()) cir.setReturnValue(CameraSubmersionType.NONE);
    }
}
