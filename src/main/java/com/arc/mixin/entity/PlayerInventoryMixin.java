
package com.arc.mixin.entity;

import com.arc.Arc;
import com.arc.interaction.managers.hotbar.HotbarManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.player.PlayerInventory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @SuppressWarnings({"MixinAnnotationTarget"})
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = Opcodes.GETFIELD))
    private int modifySelectedSlot(int original) {
        if (((PlayerInventory) ((Object) this)).player != Arc.getMc().player) return original;
        final int hotbarSlot = HotbarManager.getActiveSlot();
        if (hotbarSlot == -1) return original;
        return hotbarSlot;
    }
}
