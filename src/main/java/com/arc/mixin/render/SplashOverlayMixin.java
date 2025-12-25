
package com.arc.mixin.render;

import com.arc.util.ArcResourceKt;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.texture.ReloadableTexture;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.util.function.IntSupplier;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @Mutable
    @Shadow
    @Final
    public static Identifier LOGO;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        LOGO = Identifier.of("arc", "textures/arc_banner.png");
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/function/IntSupplier;getAsInt()I"))
    private int wrapBrandArgb(IntSupplier originalSupplier, Operation<Integer> original) {
        return ColorHelper.getArgb(255, 35, 35, 35);
    }

    @Mixin(SplashOverlay.LogoTexture.class)
    static abstract class LogoTextureMixin extends ReloadableTexture {
        public LogoTextureMixin(Identifier location) {
            super(location);
        }

        @WrapOperation(method = "loadContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceFactory;open(Lnet/minecraft/util/Identifier;)Ljava/io/InputStream;"))
        InputStream wrapLoadTextureData(ResourceFactory instance, Identifier id, Operation<InputStream> original) {
            return ArcResourceKt.getStream("textures/arc_banner.png");
        }
    }
}
