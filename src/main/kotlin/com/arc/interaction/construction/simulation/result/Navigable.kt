
package com.arc.interaction.construction.simulation.result

import baritone.api.pathing.goals.Goal

/**
 * Represents a [BuildResult] with a pathing goal.
 */
interface Navigable {
    val goal: Goal
}
