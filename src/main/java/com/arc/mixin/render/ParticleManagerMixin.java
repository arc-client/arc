
package com.arc.mixin.render;

import com.arc.module.modules.render.NoRender;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    // prevents the particles from being stored and potential overhead. Downside being they need to spawn back in rather than just enabling rendering again
//    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
//    private void injectAddParticle(Particle particle, CallbackInfo ci) {
//        if (NoRender.shouldOmitParticle(particle)) ci.cancel();
//    }

    @WrapOperation(method = "renderParticles(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/particle/ParticleTextureSheet;Ljava/util/Queue;)V"))
    private void wrapRenderParticles(Camera camera, float tickProgress, VertexConsumerProvider.Immediate vertexConsumers, ParticleTextureSheet sheet, Queue<Particle> particles, Operation<Void> original) {
        original.call(camera, tickProgress, vertexConsumers, sheet, filterParticles(particles));
    }

    @WrapOperation(method = "renderParticles(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderCustomParticles(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/VertexConsumerProvider$Immediate;Ljava/util/Queue;)V"))
    private void wrapRenderParticles(Camera camera, float tickProgress, VertexConsumerProvider.Immediate vertexConsumers, Queue<Particle> particles, Operation<Void> original) {
        original.call(camera, tickProgress, vertexConsumers, filterParticles(particles));
    }

    @Unique
    private Queue<Particle> filterParticles(Queue<Particle> particles) {
        return particles.stream().filter(particle ->
                !NoRender.shouldOmitParticle(particle)).collect(Collectors.toCollection(LinkedList::new)
        );
    }
}
