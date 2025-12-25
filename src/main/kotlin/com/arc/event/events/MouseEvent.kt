
package com.arc.event.events

import com.arc.config.settings.complex.Bind
import com.arc.event.callback.Cancellable
import com.arc.event.callback.ICancellable
import com.arc.util.math.Vec2d

sealed class MouseEvent {
    /**
     * Represents a mouse click event
     *
     * @property button The button that was clicked
     * @property action The action performed (e.g., press or release)
     * @property modifiers An integer representing any modifiers (e.g., shift or ctrl) active during the event
     */
    data class Click(
        val button: Int,
        val action: Int,
        val modifiers: Int,
    ) : ICancellable by Cancellable() {
        val isReleased = action == 0
        val isPressed = action == 1

        fun satisfies(bind: Bind) = bind.modifiers and modifiers == bind.modifiers && bind.mouse == button
    }

    /**
     * Represents a mouse scroll event
     *
     * @property delta The amount of scrolling in the x and y directions
     */
    data class Scroll(
        val delta: Vec2d,
    ) : ICancellable by Cancellable()

    /**
     * Represents a mouse move event.
     *
     * @property position The x and y position of the mouse on the screen.
     */
    data class Move(
        val position: Vec2d,
    ) : ICancellable by Cancellable()
}
