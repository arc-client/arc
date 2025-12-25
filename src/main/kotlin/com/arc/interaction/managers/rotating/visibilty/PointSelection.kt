
package com.arc.interaction.managers.rotating.visibilty

import com.arc.interaction.managers.rotating.Rotation.Companion.dist
import com.arc.interaction.managers.rotating.RotationManager
import com.arc.util.Describable
import com.arc.util.NamedEnum
import com.arc.util.math.distSq
import com.arc.util.math.times

enum class PointSelection(
    override val displayName: String,
    override val description: String,
    val select: (Collection<VisibilityChecker.CheckedHit>) -> VisibilityChecker.CheckedHit?
) : NamedEnum, Describable {
    ByRotation(
        "By Rotation",
        "Choose the point that needs the least rotation from your current view (minimal camera turn).",
        select = { hits ->
            hits.minByOrNull { RotationManager.activeRotation dist it.rotation }
        }
    ),
    Optimum(
        "Optimum",
        "Choose the point closest to the average of all candidates (balanced and stable aim).",
        select = { hits ->
            val optimum = hits
                .mapNotNull { it.hit.pos }
                .reduceOrNull { acc, pos -> acc.add(pos) }
                ?.times(1 / hits.size.toDouble())

            optimum?.let { center ->
                hits.minByOrNull { it.hit.pos?.distSq(center) ?: 0.0 }
            }
        }
    )
}
