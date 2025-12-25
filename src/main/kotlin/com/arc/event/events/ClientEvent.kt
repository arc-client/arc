
package com.arc.event.events

import com.arc.event.Event
import com.arc.event.callback.Cancellable
import com.arc.event.callback.ICancellable
import net.minecraft.client.sound.SoundInstance
import java.util.*

sealed class ClientEvent {
    /**
     * Triggered upon client initialization
     */
    class Startup : Event

    /**
     * Triggered upon client shutdown
     */
    class Shutdown : Event

    /**
     * Triggered upon game logic tick
     *
     * @property speed The speed of the timer.
     */
    data class TimerUpdate(var speed: Double) : Event

    /**
     * Triggered before playing a sound
     */
    data class Sound(val sound: SoundInstance) : ICancellable by Cancellable()

    /**
     * Represents a fixed tick event in the application.
     *
     * A fixed tick can be used to execute a specific task consistently at regular intervals, based on the provided
     * timer task. This event is part of the event system and can be subscribed to for handling the specified timer task.
     *
     * @property timerTask The task that is executed during this fixed tick event.
     */
    data class FixedTick(val timerTask: TimerTask) : Event
}
