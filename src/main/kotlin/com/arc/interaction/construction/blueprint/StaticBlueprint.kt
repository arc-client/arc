
package com.arc.interaction.construction.blueprint

import com.arc.util.extension.Structure

data class StaticBlueprint(
    override val structure: Structure,
) : Blueprint() {
    override fun toString() = "Static Blueprint at ${center?.toShortString()}"

    companion object {
        fun Structure.toBlueprint() = StaticBlueprint(this)
    }
}
