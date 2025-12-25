
package com.arc.mixin.entity;

import com.arc.Arc;
import com.arc.event.EventFlow;
import com.arc.event.events.MovementEvent;
import com.arc.interaction.managers.rotating.RotationManager;
import com.arc.module.modules.movement.Velocity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {

    @Unique private final LivingEntity arc$instance = (LivingEntity) (Object) this;

    @Shadow protected abstract float getJumpVelocity();

    /**
     * Overwrites the jump function to use our rotation and movements
     * <pre>{@code
     * protected void jump() {
     *     Vec3d vec3d = this.getVelocity();
     *     this.setVelocity(vec3d.x, (double)this.getJumpVelocity(), vec3d.z);
     *     if (this.isSprinting()) {
     *         float f = this.getYaw() * (float) (Math.PI / 180.0);
     *         this.setVelocity(this.getVelocity().add((double)(-MathHelper.sin(f) * 0.2F), 0.0, (double)(MathHelper.cos(f) * 0.2F)));
     *     }
     *
     *     this.velocityDirty = true;
     * }
     * }</pre>
     */
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    void onJump(CallbackInfo ci) {
        LivingEntity self = arc$instance;
        if (self != Arc.getMc().player) return;
        ci.cancel();

        float height = this.getJumpVelocity();
        MovementEvent.Jump event = EventFlow.post(new MovementEvent.Jump(height));

        if (event.isCanceled()) return;

        Vec3d vec3d = self.getVelocity();
        self.setVelocity(vec3d.x, event.getHeight(), vec3d.z);

        if (self.isSprinting()) {
            Float yaw = RotationManager.getMovementYaw();
            float f = ((yaw != null) ? yaw : self.getYaw()) * ((float) Math.PI / 180);
            self.setVelocity(self.getVelocity().add(-MathHelper.sin(f) * 0.2f, 0.0, MathHelper.cos(f) * 0.2f));
        }

        self.velocityDirty = true;
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        if (EventFlow.post(new MovementEvent.Entity.Pre(arc$instance, movementInput)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("TAIL"))
    void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        EventFlow.post(new MovementEvent.Entity.Post(arc$instance, movementInput));
    }

    /**
     * Modifies the entity pitch with the current rotation when the entity is fall flying
     */
    @WrapOperation(method = "calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"))
    private float hookModifyFallFlyingPitch(LivingEntity entity, Operation<Float> original) {
        Float pitch = RotationManager.getMovementPitch();
        if (entity != Arc.getMc().player || pitch == null) return original.call(entity);

        return pitch;
    }

    /**
     * Modifies the entity yaw with the active rotation yaw when the entity swing its hand
     * <pre>{@code
     * protected float turnHead(float bodyRotation, float headRotation) {
     *     float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
     *     this.bodyYaw += f * 0.3F;
     *     float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
     *     float h = this.getMaxRelativeHeadRotation();
     *     if (Math.abs(g) > h) {
     *         this.bodyYaw = this.bodyYaw + (g - (float)MathHelper.sign((double)g) * h);
     *     }
     *
     *     boolean bl = g < -90.0F || g >= 90.0F;
     *     if (bl) {
     *         headRotation *= -1.0F;
     *     }
     *
     *     return headRotation;
     * }
     * }</pre>
     */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 1)))
    private float rotBody(LivingEntity entity, Operation<Float> original) {
        if (arc$instance != Arc.getMc().player) {
            return original.call(entity);
        }

        Float yaw = RotationManager.getHeadYaw();
        return (yaw == null) ? original.call(entity) : yaw;
    }

    /**
     * Modifies the entity yaw with the active rotation yaw
     * <pre>{@code
     * protected float turnHead(float bodyRotation, float headRotation) {
     *     float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
     *     this.bodyYaw += f * 0.3F;
     *     float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
     *     float h = this.getMaxRelativeHeadRotation();
     *     if (Math.abs(g) > h) {
     *         this.bodyYaw = this.bodyYaw + (g - (float)MathHelper.sign((double)g) * h);
     *     }
     *
     *     boolean bl = g < -90.0F || g >= 90.0F;
     *     if (bl) {
     *         headRotation *= -1.0F;
     *     }
     *
     *     return headRotation;
     * }
     * }</pre>
     */
    @WrapOperation(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float rotHead(LivingEntity entity, Operation<Float> original) {
        if (arc$instance != Arc.getMc().player) {
            return original.call(entity);
        }

        Float yaw = RotationManager.getHeadYaw();
        return (yaw == null) ? original.call(entity) : yaw;
    }



    @WrapMethod(method = "pushAwayFrom")
    private void wrapPushAwayFrom(Entity entity, Operation<Void> original) {
        if (arc$instance == Arc.getMc().player &&
                Velocity.INSTANCE.isEnabled() &&
                Velocity.getPushed()) return;
        original.call(entity);
    }
}
