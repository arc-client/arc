
package com.arc.mixin.render;

import com.arc.module.modules.render.ExtraTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Shadow @Final private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "collectPlayerEntries", at = @At(value = "HEAD"), cancellable = true)
    private void onCollectPlayerEntriesHead(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        if (ExtraTab.INSTANCE.isDisabled()) return;
        if (client.player == null) return;
        cir.setReturnValue(
                client.player.networkHandler
                        .getListedPlayerListEntries()
                        .stream()
                        .sorted(ENTRY_ORDERING)
                        .limit(ExtraTab.getTabEntries())
                        .toList()
        );
    }
}
