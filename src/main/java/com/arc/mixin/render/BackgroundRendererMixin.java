
package com.arc.mixin.render;

import com.arc.module.modules.render.NoRender;
import com.arc.module.modules.render.WorldColors;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

/**
 * <pre>{@code
 * Vec3d vec3d2 = camera.getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
 * Vec3d vec3d3 = CubicSampler.sampleColor(
 *         vec3d2, (x, y, z) -> world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(biomeAccess.getBiomeForNoiseGen(x, y, z).value().getFogColor()), v)
 * );
 * red = (float)vec3d3.getX();
 * green = (float)vec3d3.getY();
 * blue = (float)vec3d3.getZ();
 * }</pre>
 */
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Shadow @Final private static List<BackgroundRenderer.StatusEffectFogModifier> FOG_MODIFIERS;

    /**
     * Modifies the fog color returned from CubicSampler.sampleColor
     */
    @ModifyExpressionValue(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
    private static Vec3d modifyFogColor(Vec3d original) {
        return WorldColors.fogOfWarColor(original);
    }

    @ModifyExpressionValue(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getWaterFogColor()I"))
    private static int modifyWaterFogColor(int original) {
        return WorldColors.waterFogColor(original);
    }

    @Inject(method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", at = @At("HEAD"), cancellable = true)
    private static void injectFogModifier(Entity entity, float tickProgress, CallbackInfoReturnable<BackgroundRenderer.StatusEffectFogModifier> cir){
        if (entity instanceof LivingEntity livingEntity) {
            Stream<BackgroundRenderer.StatusEffectFogModifier> modifiers = FOG_MODIFIERS
                    .stream()
                    .filter((modifier) ->
                            modifier.shouldApply(livingEntity, tickProgress) && NoRender.shouldAcceptFog(modifier)
                    );
            cir.setReturnValue(modifiers.findFirst().orElse(null));
        }
    }
}
