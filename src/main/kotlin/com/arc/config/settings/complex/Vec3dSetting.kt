
package com.arc.config.settings.complex

import com.google.gson.reflect.TypeToken
import com.arc.brigadier.argument.double
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.math.Vec3d

class Vec3dSetting(defaultValue: Vec3d) : SettingCore<Vec3d>(
	defaultValue,
	TypeToken.get(Vec3d::class.java).type
) {
    context(setting: Setting<*, Vec3d>)
	override fun ImGuiBuilder.buildLayout() {
        inputVec3d(setting.name, ::value as Vec3d) // FixMe: what the fuck
        arcTooltip(setting.description)
    }

	context(setting: Setting<*, Vec3d>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(double("X", -30000000.0, 30000000.0)) { x ->
            required(double("Y", -64.0, 255.0)) { y ->
                required(double("Z", -30000000.0, 30000000.0)) { z ->
                    execute {
                        setting.trySetValue(Vec3d(x().value(), y().value(), z().value()))
                    }
                }
            }
        }
    }
}
