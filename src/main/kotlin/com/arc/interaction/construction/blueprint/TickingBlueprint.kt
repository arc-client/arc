
package com.arc.interaction.construction.blueprint

import com.arc.context.SafeContext
import com.arc.threading.runSafe
import com.arc.util.extension.Structure
import net.minecraft.util.math.Vec3i

data class TickingBlueprint(
    val onTick: SafeContext.(Structure) -> Structure? = { it },
) : Blueprint() {
    fun tick() =
        runSafe {
            onTick(structure)?.also { new ->
                structure = new
            }
        }

    override var structure: Structure = emptyMap()
        private set(value) {
            field = value
            bounds.update()
        }

    override fun toString() = "Dynamic Blueprint at ${center?.toShortString()}"

    companion object {
        fun offset(offset: Vec3i): SafeContext.(Structure) -> Structure? = {
            it.map { (pos, state) ->
                pos.add(offset) to state
            }.toMap()
        }

        fun tickingBlueprint(
            onTick: SafeContext.(Structure) -> Structure?,
        ) = TickingBlueprint(onTick)
    }
}
