
package com.arc.mixin;

import com.arc.Arc;
import com.arc.config.Setting;
import com.arc.module.Module;
import com.arc.module.ModuleRegistry;
import com.arc.util.DynamicExceptionKt;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

// Modify the crash report behavior for dynamic remapping and GitHub issue link
@Mixin(CrashReport.class)
public class CrashReportMixin {
    @Mutable
    @Shadow @Final private Throwable cause;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/Throwable;)V", at = @At("TAIL"))
    void injectConstructor(String message, Throwable cause, CallbackInfo ci) {
        if (!Arc.INSTANCE.isDebug() && MinecraftClient.getInstance() != null) {
            this.cause = DynamicExceptionKt.dynamicException(cause);
        }
    }

    @WrapMethod(method = "asString(Lnet/minecraft/util/crash/ReportType;Ljava/util/List;)Ljava/lang/String;")
    String injectString(ReportType type, List<String> extraInfo, Operation<String> original) {
        var list = new ArrayList<>(extraInfo);
        list.add("If this issue is related to Arc, check if other users have experienced this too, or create a new issue at " + Arc.REPO_URL + "/issues.\n\n");

        if (MinecraftClient.getInstance() != null) {
            list.add("Enabled modules:");

            ModuleRegistry.INSTANCE.getModules()
                    .stream()
                    .filter(Module::isEnabled)
                    .forEach(module -> {
                        list.add(String.format("\t%s", module.getName()));

                        module.getSettings()
                                .stream()
                                .filter(Setting::isModified)
                                .forEach(setting -> list.add(String.format("\t\t%s -> %s", setting.getName(), setting.getValue())));
                    });
        }

        list.add("\n" + "-".repeat(43) + "\n");

        return original.call(type, list);
    }
}
