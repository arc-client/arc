
package com.arc.event.events

import com.arc.event.Event

sealed class GuiEvent {
    /**
     * Triggered when a new ImGui frame is created and the client
     * is allowed to submit any command from this point until [EndFrame].
     */
    data object NewFrame : Event

    /**
     * Triggered when the previous ImGui frame is ended and the client
     * is able to perform OpenGL calls.
     *
     * By default, the game's framebuffer is bound.
     */
    data object EndFrame : Event
}
