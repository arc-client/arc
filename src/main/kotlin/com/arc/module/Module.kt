
package com.arc.module

import com.arc.Arc
import com.arc.command.ArcCommand
import com.arc.config.Configurable
import com.arc.config.Configuration
import com.arc.config.MutableAutomationConfig
import com.arc.config.MutableAutomationConfigImpl
import com.arc.config.SettingCore
import com.arc.config.configurations.ModuleConfigs
import com.arc.config.settings.complex.Bind
import com.arc.context.SafeContext
import com.arc.event.Muteable
import com.arc.event.events.ClientEvent
import com.arc.event.events.ConnectionEvent
import com.arc.event.events.KeyboardEvent
import com.arc.event.events.MouseEvent
import com.arc.event.listener.Listener
import com.arc.event.listener.SafeListener
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.event.listener.UnsafeListener
import com.arc.module.tag.ModuleTag
import com.arc.sound.ArcSound
import com.arc.sound.SoundManager.play
import com.arc.util.KeyCode
import com.arc.util.Nameable

/**
 * A [Module] is a feature or tool for the utility mod.
 * It represents a [Configurable] component of the mod,
 * with its own set of behaviors and properties.
 *
 * Each [Module] has a [name], which is displayed in-game.
 * The [description] of the module is shown when hovering over
 * the [ModuleButton] in the GUI and in [ArcCommand]s.
 * The [Module] can be associated with a [Set] of [ModuleTag]s to allow for
 * easier filtering and searching in the GUI.
 *
 * A [Module] can be activated by a [keybind], represented by a [KeyCode].
 * The default [keybind] is the key on which
 * the module will be activated by default.
 * If a module does not need to be activated by a key (like [ClickGui]),
 * the default [keybind] should not be set (using [KeyCode.Unbound]).
 *
 * [Module]s are [Configurable]s with [settings] (see [SettingCore] for all setting types).
 * Example:
 * ```
 * private val foo by setting("Foo", true)
 * private val bar by setting("Bar", 0.0, 0.1..5.0, 0.1)
 * ```
 *
 * These settings are persisted in the `arc/config/modules.json` config file.
 * See [ModuleConfigs.primary] and [Configuration] for more details.
 *
 * In the `init` block, you can add hooks like [onEnable], [onDisable], [onToggle] and add listeners.
 *
 * Example:
 * ```
 * init {
 *     onEnable { // runs on module activation
 *         LOG.info("I was enabled!")
 *     }
 *
 *     onToggle { to ->
 *          LOG.info("Module enabled: ${to}")
 *     }
 *
 *     onDisable {
 *          LOG.info("I was disable!")
 *     }
 *
 *     listener<TickEvent.Pre> { event ->
 *         LOG.info("I've ticked!")
 *     }
 * }
 * ```
 *
 * [Listener]s are only triggered if:
 * - [Module] is [isEnabled], otherwise it [isMuted] (see [Muteable])
 * - [Module] was configured to [alwaysListening]
 * - [Listener] was configured to [Listener.alwaysListen]
 *
 * Example:
 * ```
 * val bind1 = setting("Keybind", KeyCode.A)
 * val bind2 = setting("Keybind", Bind(KeyCode.A.code, 0, -1))
 *
 * listen<KeyboardEvent.Press>(alwaysListen = true) { event ->
 *     if (!event.satisfies(bind1) || !event.satisfies(bind2)) return@listen
 *
 *     if (event.isPressed) toggle()
 *     else if (event.isReleased) disable()
 * }
 * ```
 *
 * See [SafeListener] and [UnsafeListener] for more details.
 */
abstract class Module(
    override val name: String,
    val description: String = "",
    val tag: ModuleTag,
    private val alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false,
    defaultKeybind: Bind = Bind.EMPTY,
    autoDisable: Boolean = false
) : Nameable, Muteable, Configurable(ModuleConfigs), MutableAutomationConfig by MutableAutomationConfigImpl() {
    private val isEnabledSetting = setting("Enabled", enabledByDefault) { false }
    val keybindSetting = setting("Keybind", defaultKeybind) { false }
    val disableOnReleaseSetting = setting("Disable On Release", false) { false }
    val drawSetting = setting("Draw", true, "Draws the module in the module list hud element")

    var isEnabled by isEnabledSetting
    val isDisabled get() = !isEnabled

    val keybind by keybindSetting
    val disableOnRelease by disableOnReleaseSetting
    val draw by drawSetting

    override val isMuted: Boolean
        get() = !isEnabled && !alwaysListening

    init {
        listen<KeyboardEvent.Press>(alwaysListen = true) { event ->
            if (mc.options.commandKey.isPressed
                || Arc.mc.currentScreen != null
                || !event.satisfies(keybind)) return@listen

            if (event.isPressed && !event.isRepeated) toggle()
            else if (event.isReleased && disableOnRelease) disable()
        }

        listen<MouseEvent.Click>(alwaysListen = true) { event ->
            if (mc.options.commandKey.isPressed
                || mc.currentScreen != null
                || !event.satisfies(keybind)) return@listen

            if (event.isPressed) toggle()
            else if (event.isReleased && disableOnRelease) disable()
        }

        onEnable { ArcSound.ModuleOn.play() }
        onDisable { ArcSound.ModuleOff.play() }

        onEnableUnsafe { ArcSound.ModuleOn.play() }
        onDisableUnsafe { ArcSound.ModuleOff.play() }

        listen<ClientEvent.Shutdown> { if (autoDisable) disable() }
        listen<ClientEvent.Startup> { if (autoDisable) disable() }
        listen<ConnectionEvent.Disconnect> { if (autoDisable) disable() }
    }

    fun enable() {
        isEnabled = true
    }

    fun disable() {
        isEnabled = false
    }

    fun toggle() {
        isEnabled = !isEnabled
    }

    protected fun onEnable(block: SafeContext.() -> Unit) {
        isEnabledSetting.onValueChange { from, to ->
            if (!from && to) block()
        }
    }

    protected fun onDisable(block: SafeContext.() -> Unit) {
        isEnabledSetting.onValueChange { from, to ->
            if (from && !to) block()
        }
    }

    protected fun onToggle(block: SafeContext.(to: Boolean) -> Unit) {
        isEnabledSetting.onValueChange { from, to ->
            if (from != to) block(to)
        }
    }

    protected fun onEnableUnsafe(block: () -> Unit) {
        isEnabledSetting.onValueChangeUnsafe { from, to ->
            if (!from && to) block()
        }
    }

    protected fun onDisableUnsafe(block: () -> Unit) {
        isEnabledSetting.onValueChangeUnsafe { from, to ->
            if (from && !to) block()
        }
    }

    protected fun onToggleUnsafe(block: (to: Boolean) -> Unit) {
        isEnabledSetting.onValueChangeUnsafe { from, to ->
            if (from != to) block(to)
        }
    }
}
