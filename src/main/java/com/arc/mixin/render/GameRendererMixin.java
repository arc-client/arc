
package com.arc.mixin.render;

import com.arc.event.EventFlow;
import com.arc.event.events.RenderEvent;
import com.arc.graphics.RenderMain;
import com.arc.module.modules.render.NoRender;
import com.arc.module.modules.render.Zoom;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "updateCrosshairTarget(F)V", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        if (EventFlow.post(new RenderEvent.UpdateTarget()).isCanceled()) {
            info.cancel();
        }
    }

    /**
     * Begins our 3d render after the game has rendered the world
     * <pre>{@code
     * float m = Math.max(h, (float)(Integer)this.client.options.getFov().getValue());
     * Matrix4f matrix4f2 = this.getBasicProjectionMatrix(m);
     * RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.PERSPECTIVE);
     * Quaternionf quaternionf = camera.getRotation().conjugate(new Quaternionf());
     * Matrix4f matrix4f3 = (new Matrix4f()).rotation(quaternionf);
     * this.client.worldRenderer.setupFrustum(camera.getPos(), matrix4f3, matrix4f2);
     * this.client.worldRenderer.render(this.pool, renderTickCounter, bl, camera, this, matrix4f3, matrix4f);
     * }</pre>
     */
    @WrapOperation(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    void onRenderWorld(WorldRenderer instance, ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, Operation<Void> original) {
        original.call(instance, allocator, tickCounter, renderBlockOutline, camera, gameRenderer, positionMatrix, projectionMatrix);

        RenderMain.render3D(positionMatrix, projectionMatrix);
    }

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
    private float modifyMax(float original) {
        return (NoRender.INSTANCE.isEnabled() && NoRender.getNoNausea()) ? 0 : original;
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void injectShowFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.getNoFloatingItemAnimation()) ci.cancel();
    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float modifyGetFov(float original) {
        return original / Zoom.getLerpedZoom();
    }
}
