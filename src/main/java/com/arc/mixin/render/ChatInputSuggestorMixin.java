
package com.arc.mixin.render;

import com.google.common.base.Strings;
import com.arc.command.CommandManager;
import com.arc.graphics.renderer.gui.font.core.ArcAtlas;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Unique private static final Pattern COLON_PATTERN = Pattern.compile("(:[a-zA-Z0-9_]+)");
    @Unique private static final Pattern EMOJI_PATTERN = Pattern.compile("(:)([a-zA-Z0-9_]+)(:)");
    @Shadow @Final TextFieldWidget textField;
    @Shadow private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void show(boolean narrateFirstSuggestion);

    @ModifyVariable(method = "refresh", at = @At(value = "STORE"), index = 3)
    private boolean refreshModify(boolean showCompletions) {
        return CommandManager.INSTANCE.isCommand(textField.getText());
    }

    @WrapOperation(method = "refresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;getCommandDispatcher()Lcom/mojang/brigadier/CommandDispatcher;"))
    private CommandDispatcher<CommandSource> wrapRefresh(ClientPlayNetworkHandler instance, Operation<CommandDispatcher<CommandSource>> original) {
        return CommandManager.INSTANCE.currentDispatcher(textField.getText());
    }

    @Unique
    private int neoArc$getLastColon(String input) {
        if (Strings.isNullOrEmpty(input)) return -1;

        int i = -1;
        Matcher matcher = COLON_PATTERN.matcher(input);

        while (matcher.find()) {
            i = matcher.start();
        }

        return i;
    }
}
