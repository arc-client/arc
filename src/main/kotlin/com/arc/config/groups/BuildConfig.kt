
package com.arc.config.groups

import com.arc.config.ISettingGroup
import com.arc.interaction.managers.rotating.visibilty.PointSelection
import com.arc.util.Describable
import com.arc.util.NamedEnum

interface BuildConfig : ISettingGroup {
    // General
    val pathing: Boolean
    val stayInRange: Boolean
    val collectDrops: Boolean
    val spleefEntities: Boolean
    val maxPendingActions: Int
    val actionTimeout: Int
    val maxBuildDependencies: Int

    val entityReach: Double
    val blockReach: Double
    val scanReach: Double

    val checkSideVisibility: Boolean
    val strictRayCast: Boolean
    val resolution: Int
    val pointSelection: PointSelection

    enum class SwingType(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        Vanilla("Vanilla", "Play the hand swing locally and also notify the server (default, looks and works as expected)."),
        Server("Server", "Only notify the server to swing; local animation may not play unless the server echoes it."),
        Client("Client", "Only play the local swing animation; does not notify the server (purely visual).")
    }
}
