
package com.arc.interaction.managers.interacting

import com.arc.config.ISettingGroup
import com.arc.config.groups.ActionConfig
import com.arc.config.groups.BuildConfig
import com.arc.util.Describable
import com.arc.util.NamedEnum

interface InteractConfig : ActionConfig, ISettingGroup {
    val rotate: Boolean
    val airPlace: AirPlaceMode
    val axisRotateSetting: Boolean
    val axisRotate get() = rotate && airPlace.isEnabled && axisRotateSetting
    val interactConfirmationMode: InteractConfirmationMode
    val interactDelay: Int
    val interactionsPerTick: Int
    val swing: Boolean
    val swingType: BuildConfig.SwingType
    val sounds: Boolean

    enum class AirPlaceMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        None("None", "Do not attempt air placements; only place against valid supports."),
        Standard("Standard", "Try common air-place techniques for convenience; moderate compatibility."),
        Grim("Grim", "Use grim specific air placing.")
        ;

        val isEnabled get() = this != None
    }

    enum class InteractConfirmationMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        None("No confirmation", "Interact immediately without waiting for the server; possible desync."),
        PlaceThenAwait("Interact now, confirm later", "Interact immediately, then wait for server confirmation to verify."),
        AwaitThenPlace("Confirm first, then Interact", "Wait for server response before interacting; safest, adds a short delay.")
    }
}
