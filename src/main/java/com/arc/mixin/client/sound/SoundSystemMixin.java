
package com.arc.mixin.client.sound;

import com.arc.event.EventFlow;
import com.arc.event.events.ClientEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void onPlay(SoundInstance sound, CallbackInfo ci) {
        if (EventFlow.post(new ClientEvent.Sound(sound)).isCanceled()) {
            ci.cancel();
        }
    }
}
