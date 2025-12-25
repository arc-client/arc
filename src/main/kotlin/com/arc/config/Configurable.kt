
package com.arc.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.arc.Arc.LOG
import com.arc.config.Configuration.Companion.configurables
import com.arc.config.settings.CharSetting
import com.arc.config.settings.FunctionSetting
import com.arc.config.settings.StringSetting
import com.arc.config.settings.collections.BlockCollectionSetting
import com.arc.config.settings.collections.ClassCollectionSetting
import com.arc.config.settings.collections.CollectionSetting
import com.arc.config.settings.collections.ItemCollectionSetting
import com.arc.config.settings.collections.MapSetting
import com.arc.config.settings.comparable.BooleanSetting
import com.arc.config.settings.comparable.EnumSetting
import com.arc.config.settings.complex.Bind
import com.arc.config.settings.complex.BlockPosSetting
import com.arc.config.settings.complex.BlockSetting
import com.arc.config.settings.complex.ColorSetting
import com.arc.config.settings.complex.KeybindSettingCore
import com.arc.config.settings.complex.Vec3dSetting
import com.arc.config.settings.numeric.DoubleSetting
import com.arc.config.settings.numeric.FloatSetting
import com.arc.config.settings.numeric.IntegerSetting
import com.arc.config.settings.numeric.LongSetting
import com.arc.util.Communication.logError
import com.arc.util.KeyCode
import com.arc.util.Nameable
import imgui.flag.ImGuiInputTextFlags
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color

/**
 * Represents a set of [SettingCore]s that are associated with the [name] of the [Configurable].
 * The settings are managed by this [Configurable] and are saved and loaded as part of the [Configuration].
 *
 * This class also provides a series of helper methods ([setting]) for creating different types of settings.
 *
 * @property settings A set of [SettingCore]s that this configurable manages.
 */
abstract class Configurable(
    val configuration: Configuration,
) : Jsonable, Nameable {
    val settings = mutableListOf<Setting<*, *>>()
	val settingGroups = mutableListOf<SettingGroup>()

    init {
        registerConfigurable()
    }

    private fun registerConfigurable() {
		if (configurables.any { it.name == name })
			throw IllegalStateException("Configurable with name $name already exists")
		configuration.configurables.add(this)
    }

    fun <T : SettingCore<R>, R : Any> Setting<T, R>.register() = apply {
        if (settings.any { it.name == name })
            throw IllegalStateException("Setting with name $name already exists for configurable: ${this@Configurable.name}")
	    settings.add(this)
    }

    override fun toJson() =
        JsonObject().apply {
            settings.forEach { setting ->
                try {
                    add(setting.name, setting.toJson())
                } catch (e: Exception) {
                    logError("Failed to serialize $setting in ${this::class.simpleName}", e)
                }
            }
        }

    override fun loadFromJson(serialized: JsonElement) {
        serialized.asJsonObject.entrySet().forEach { (name, value) ->
            settings.find { it.name == name }?.loadFromJson(value)
                ?: LOG.warn("No saved setting found for $name with $value in ${this::class.simpleName}")
        }
    }

    fun setting(
        name: String,
        defaultValue: Boolean,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, BooleanSetting(defaultValue), this, visibility).register()

    inline fun <reified T : Enum<T>> setting(
        name: String,
        defaultValue: T,
        description: String = "",
        noinline
        visibility: () -> Boolean = { true },
    ) = Setting(name, description,EnumSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Char,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, CharSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: String,
        multiline: Boolean = false,
        flags: Int = ImGuiInputTextFlags.None,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, StringSetting(defaultValue, multiline, flags), this, visibility).register()

	@JvmName("collectionSetting1")
    fun setting(
        name: String,
        defaultValue: Collection<Block>,
        immutableCollection: Collection<Block> = Registries.BLOCK.toList(),
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, BlockCollectionSetting(immutableCollection, defaultValue.toMutableList()), this, visibility).register()

	@JvmName("collectionSetting2")
    fun setting(
        name: String,
        defaultValue: Collection<Item>,
        immutableCollection: Collection<Item> = Registries.ITEM.toList(),
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, ItemCollectionSetting(immutableCollection, defaultValue.toMutableList()), this, visibility).register()

	@JvmName("collectionSetting3")
    inline fun <reified T : Comparable<T>> setting(
        name: String,
        defaultValue: Collection<T>,
        immutableList: Collection<T> = defaultValue,
        description: String = "",
        noinline visibility: () -> Boolean = { true },
    ) = Setting(
	    name,
	    description,
	    CollectionSetting(
		    defaultValue.toMutableList(),
		    immutableList,
		    TypeToken.getParameterized(Collection::class.java, T::class.java).type
		),
		this,
	    visibility
	).register()

	@JvmName("collectionSetting4")
    inline fun <reified T : Any> setting(
	    name: String,
	    defaultValue: Collection<T>,
	    immutableList: Collection<T> = defaultValue,
	    description: String = "",
	    noinline visibility: () -> Boolean = { true },
    ) = Setting(name, description, ClassCollectionSetting(immutableList, defaultValue.toMutableList()), this, visibility).register()

    // ToDo: Actually implement maps
    inline fun <reified K : Any, reified V : Any> setting(
        name: String,
        defaultValue: Map<K, V>,
        description: String = "",
        noinline visibility: () -> Boolean = { true },
    ) = Setting(
	    name,
	    description,
	    MapSetting(
		    defaultValue.toMutableMap(),
		    TypeToken.getParameterized(MutableMap::class.java, K::class.java, V::class.java).type
		),
	    this,
		visibility
	).register()

    fun setting(
        name: String,
        defaultValue: Double,
        range: ClosedRange<Double>,
        step: Double = 1.0,
        description: String = "",
        unit: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, DoubleSetting(defaultValue, range, step, unit), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Float,
        range: ClosedRange<Float>,
        step: Float = 1f,
        description: String = "",
        unit: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, FloatSetting(defaultValue, range, step, unit), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Int,
        range: ClosedRange<Int>,
        step: Int = 1,
        description: String = "",
        unit: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, IntegerSetting(defaultValue, range, step, unit), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Long,
        range: ClosedRange<Long>,
        step: Long = 1,
        description: String = "",
        unit: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, LongSetting(defaultValue, range, step, unit), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Bind,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, KeybindSettingCore(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: KeyCode,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, KeybindSettingCore(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Color,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, ColorSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Vec3d,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, Vec3dSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: BlockPos.Mutable,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, BlockPosSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: BlockPos,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, BlockPosSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: Block,
        description: String = "",
        visibility: () -> Boolean = { true },
    ) = Setting(name, description, BlockSetting(defaultValue), this, visibility).register()

    fun setting(
        name: String,
        defaultValue: () -> Unit,
        description: String = "",
        visibility: () -> Boolean = { true }
    ) = Setting(name, description, FunctionSetting(defaultValue), this, visibility).register()
}