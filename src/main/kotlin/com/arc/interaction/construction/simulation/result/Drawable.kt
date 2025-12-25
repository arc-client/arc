
package com.arc.interaction.construction.simulation.result

import com.arc.graphics.renderer.esp.ShapeBuilder

/**
 * Represents a [BuildResult] that can be rendered in-game.
 */
interface Drawable {
    fun ShapeBuilder.buildRenderer()
}
