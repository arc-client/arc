
package com.arc.config.settings.complex

import com.google.gson.reflect.TypeToken
import com.arc.brigadier.argument.blockState
import com.arc.brigadier.argument.value
import com.arc.brigadier.execute
import com.arc.brigadier.required
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.util.extension.CommandBuilder
import net.minecraft.block.Block
import net.minecraft.command.CommandRegistryAccess

/**
 * @see [com.arc.config.Configurable]
 */
class BlockSetting(defaultValue: Block) : SettingCore<Block>(
	defaultValue,
	TypeToken.get(Block::class.java).type
) {
	context(setting: Setting<*, Block>)
    override fun ImGuiBuilder.buildLayout() {}

	context(setting: Setting<*, Block>)
    override fun CommandBuilder.buildCommand(registry: CommandRegistryAccess) {
        required(blockState(setting.name, registry)) { argument ->
            execute {
                setting.trySetValue(argument().value().blockState.block)
            }
        }
    }
}
