
package com.arc.mixin.render;

import com.arc.module.modules.render.NoRender;
import net.minecraft.client.render.WorldBorderRendering;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorderRendering.class)
public class WorldBorderRenderingMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void injectRender(WorldBorder border, Vec3d cameraPos, double viewDistanceBlocks, double farPlaneDistance, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.getNoWorldBorder()) ci.cancel();
    }
}
