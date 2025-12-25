
package com.arc.mixin.input;

import com.arc.event.EventFlow;
import com.arc.event.events.KeyboardEvent;
import com.arc.module.modules.player.InventoryMove;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Keyboard;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @WrapMethod(method = "onKey")
    private void onKey(long window, int key, int scancode, int action, int modifiers, Operation<Void> original) {
        EventFlow.post(new KeyboardEvent.Press(key, scancode, action, modifiers));
        original.call(window, key, scancode, action, modifiers);
    }

    @Inject(method = "onKey", at = @At("RETURN"))
    private void onKeyTail(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!InventoryMove.getShouldMove() || !InventoryMove.isKeyMovementRelated(key)) return;
        InputUtil.Key fromCode = InputUtil.fromKeyCode(key, scancode);
        KeyBinding.setKeyPressed(fromCode, action != 0);
    }

    @WrapMethod(method = "onChar")
    private void onChar(long window, int codePoint, int modifiers, Operation<Void> original) {
        char[] chars = Character.toChars(codePoint);

        for (char c : chars)
            EventFlow.post(new KeyboardEvent.Char(c));

        original.call(window, codePoint, modifiers);
    }
}
