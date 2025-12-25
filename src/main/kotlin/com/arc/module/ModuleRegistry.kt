
package com.arc.module

import com.arc.core.Loadable
import com.arc.util.reflections.getInstances

object ModuleRegistry : Loadable {
    override val priority = 1

    val modules = getInstances<Module>()
        .sortedBy { it.name }

    val moduleNameMap = modules.associateBy { it.name }

    override fun load() =
        "Loaded ${modules.size} modules with ${modules.sumOf { it.settings.size }} settings"
}
