
package com.arc.config.settings.complex

import com.google.gson.reflect.TypeToken
import com.arc.brigadier.argument.integer
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.BlockUtils.blockPos
import com.arc.util.extension.CommandBuilder
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.math.BlockPos

/**
 * @see [com.arc.config.Configurable]
 */
class BlockPosSetting(defaultValue: BlockPos) : SettingCore<BlockPos>(
	defaultValue,
	TypeToken.get(BlockPos::class.java).type
) {
	context(setting: Setting<*, BlockPos>)
    override fun ImGuiBuilder.buildLayout() {
        inputVec3i(setting.name, value) { value = it.blockPos }
        arcTooltip(setting.description)
    }

	context(setting: Setting<*, BlockPos>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(integer("X", -30000000, 30000000)) { x ->
            required(integer("Y", -64, 255)) { y ->
                required(integer("Z", -30000000, 30000000)) { z ->
                    execute {
                        setting.trySetValue(BlockPos(x().value(), y().value(), z().value()))
                    }
                }
            }
        }
    }
}
