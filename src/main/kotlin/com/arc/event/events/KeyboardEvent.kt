
package com.arc.event.events

import com.arc.config.settings.complex.Bind
import com.arc.event.Event
import com.arc.util.KeyCode
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFW.GLFW_REPEAT

sealed class KeyboardEvent {
    /**
     * Represents a key press
     *
     * @property keyCode The key code of the key that was pressed
     * @property scanCode The scan code of the key that was pressed
     * @property action The action that was performed on the key (Pressed, Released)
     * @property modifiers The modifiers that were active when the key was pressed
     *
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/inputdev/about-keyboard-input#keyboard-input-model">About Keyboards</a>
     */
    data class Press(
        val keyCode: Int,
        val scanCode: Int,
        val action: Int,
        val modifiers: Int,
    ) : Event {
        val bind: Bind
            get() = Bind(translated.code, modifiers, -1)

        val translated: KeyCode
            get() = KeyCode.virtualMapUS(keyCode, scanCode)

        val isPressed = action >= GLFW_PRESS
        val isReleased = action == GLFW_RELEASE
        val isRepeated = action == GLFW_REPEAT

        fun satisfies(bind: Bind) = bind.key == translated.code && bind.modifiers and modifiers == bind.modifiers
    }

    /**
     * Represents glfwSetCharCallback events
     *
     * Keys and characters do not map 1:1.
     * A single key press may produce several characters, and a single
     * character may require several keys to produce
     */
    data class Char(val char: kotlin.Char) : Event
}
