
package com.arc.config.groups

import com.arc.config.Configurable
import com.arc.config.SettingGroup
import com.arc.event.events.TickEvent
import com.arc.event.events.TickEvent.Companion.ALL_STAGES
import com.arc.interaction.managers.interacting.InteractConfig
import com.arc.interaction.managers.interacting.InteractConfig.AirPlaceMode
import com.arc.interaction.managers.interacting.InteractConfig.InteractConfirmationMode
import com.arc.util.NamedEnum

class InteractSettings(
    c: Configurable,
    baseGroup: NamedEnum
) : SettingGroup(c), InteractConfig {
    override val rotate by c.setting("Rotate For Interact", true, "Rotate towards block while placing").group(baseGroup).index()
    override val airPlace by c.setting("Air Place", AirPlaceMode.None, "Allows for placing blocks without adjacent faces").group(baseGroup).index()
    override val axisRotateSetting by c.setting("Axis Rotate", true, "Overrides the Rotate For Place setting and rotates the player on each axis to air place rotational blocks") { airPlace.isEnabled }.group(baseGroup).index()
    override val sorter by c.setting("Interaction Sorter", ActionConfig.SortMode.Tool, "The order in which placements are performed").group(baseGroup).index()
    override val tickStageMask by c.setting("Interaction Stage Mask", setOf(TickEvent.Input.Post), ALL_STAGES.toSet(), description = "The sub-tick timing at which place actions are performed").group(baseGroup).index()
    override val interactConfirmationMode by c.setting("Interact Confirmation", InteractConfirmationMode.PlaceThenAwait, "Wait for block placement confirmation").group(baseGroup).index()
    override val interactDelay by c.setting("Interact Delay", 0, 0..3, 1, "Tick delay between interacting with another block").group(baseGroup).index()
    override val interactionsPerTick by c.setting("Interactions Per Tick", 1, 1..30, 1, "Maximum instant block places per tick").group(baseGroup).index()
    override val swing by c.setting("Swing On Interact", true, "Swings the players hand when placing").group(baseGroup).index()
    override val swingType by c.setting("Interact Swing Type", BuildConfig.SwingType.Vanilla, "The style of swing") { swing }.group(baseGroup).index()
    override val sounds by c.setting("Place Sounds", true, "Plays the placing sounds").group(baseGroup).index()
}