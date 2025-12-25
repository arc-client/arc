
package com.arc.mixin.entity;

import com.arc.Arc;
import com.arc.event.EventFlow;
import com.arc.event.events.EntityEvent;
import com.arc.event.events.PlayerEvent;
import com.arc.interaction.managers.rotating.RotationManager;
import com.arc.interaction.managers.rotating.RotationMode;
import com.arc.module.modules.player.RotationLock;
import com.arc.module.modules.render.NoRender;
import com.arc.util.math.Vec2d;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public void move(MovementType movementType, Vec3d movement) {
    }

    @Shadow
    public abstract float getYaw();

    /**
     * Modifies the player yaw when there is an active rotation to apply the player velocity correctly
     */
    @WrapOperation(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw()F"))
    public float velocityYaw(Entity entity, Operation<Float> original) {
        if ((Object) this != Arc.getMc().player) return original.call(entity);

        Float y = RotationManager.getMovementYaw();
        if (y == null) return original.call(entity);

        return y;
    }

    /**
     * Modifies the player yaw for the given tick delta for interpolation when there is an active rotation
     * <pre>{@code
     * public final Vec3d getRotationVec(float tickDelta) {
     *     return this.getRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
     * }
     * }</pre>
     */
    @WrapOperation(method = "getRotationVec", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw(F)F"))
    float fixDirectionYaw(Entity entity, float tickDelta, Operation<Float> original) {
        Vec2d rot = RotationManager.getRotationForVector(tickDelta);
        if (entity != Arc.getMc().player || rot == null) return original.call(entity, tickDelta);

        return (float) rot.getX();
    }

    /**
     * Modifies the player pitch for the given tick delta for interpolation when there is an active rotation
     * <pre>{@code
     * public final Vec3d getRotationVec(float tickDelta) {
     *     return this.getRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
     * }
     * }</pre>
     */
    @WrapOperation(method = "getRotationVec", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPitch(F)F"))
    float fixDirectionPitch(Entity entity, float tickDelta, Operation<Float> original) {
        Vec2d rot = RotationManager.getRotationForVector(tickDelta);
        if (entity != Arc.getMc().player || rot == null) return original.call(entity, tickDelta);

        return (float) rot.getY();
    }

    /**
     * Modifies the player yaw for the current rotation yaw
     * <pre>{@code
     * public Vec3d getRotationVector() {
     * 	return this.getRotationVector(this.getPitch(), this.getYaw());
     * }
     * }</pre>
     */
    @WrapOperation(method = "getRotationVector()Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw()F"))
    float fixDirectionYaw2(Entity entity, Operation<Float> original) {
        Vec2d rot = RotationManager.getRotationForVector(1.0);
        if (entity != Arc.getMc().player || rot == null) return original.call(entity);

        return (float) rot.getX();
    }

    /**
     * Modifies the player yaw for the current rotation pitch
     * <pre>{@code
     * public Vec3d getRotationVector() {
     * 	return this.getRotationVector(this.getPitch(), this.getYaw());
     * }
     * }</pre>
     */
    @WrapOperation(method = "getRotationVector()Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPitch()F"))
    float fixDirectionPitch2(Entity entity, Operation<Float> original) {
        Vec2d rot = RotationManager.getRotationForVector(1.0);
        if (entity != Arc.getMc().player || rot == null) return original.call(entity);

        return (float) rot.getY();
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (EventFlow.post(new PlayerEvent.ChangeLookDirection(cursorDeltaX, cursorDeltaY)).isCanceled()) ci.cancel();
    }

    @Inject(method = "onTrackedDataSet(Lnet/minecraft/entity/data/TrackedData;)V", at = @At("TAIL"))
    public void onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        EventFlow.post(new EntityEvent.Update(entity, data));
    }

    @ModifyExpressionValue(method = "isInvisible", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getFlag(I)Z"))
    private boolean modifyGetFlagInvisible(boolean original) {
        return (NoRender.INSTANCE.isDisabled() || !NoRender.getNoInvisibility()) && original;
    }

    @ModifyExpressionValue(method = "isGlowing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getFlag(I)Z"))
    private boolean modifyGetFlagGlowing(boolean original) {
        return (NoRender.INSTANCE.isDisabled() || !NoRender.getNoGlow()) && original;
    }

    @WrapWithCondition(method = "changeLookDirection", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setYaw(F)V"))
    private boolean wrapSetYaw(Entity instance, float yaw) {
        return (instance != Arc.getMc().player ||
                RotationLock.INSTANCE.isDisabled() ||
                RotationLock.INSTANCE.getRotationConfig().getRotationMode() != RotationMode.Lock ||
                RotationLock.getYawMode() == RotationLock.Mode.None);
    }

    @WrapWithCondition(method = "changeLookDirection", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPitch(F)V"))
    private boolean wrapSetPitch(Entity instance, float yaw) {
        return (instance != Arc.getMc().player ||
                RotationLock.INSTANCE.isDisabled() ||
                RotationLock.INSTANCE.getRotationConfig().getRotationMode() != RotationMode.Lock ||
                RotationLock.getPitchMode() == RotationLock.Mode.None);
    }
}
