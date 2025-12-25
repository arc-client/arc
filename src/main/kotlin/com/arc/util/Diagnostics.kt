
package com.arc.util

import com.arc.module.ModuleRegistry.modules

object Diagnostics {
    // ToDo: Expand this to include more information like version, etc.
    fun gatherDiagnostics() = buildString {
        modules.filter { it.isEnabled }
            .forEach { module ->
                append("\t${module.name}")
                module.settings
                    .filter { it.isModified }
                    .forEach { setting ->
                        append("\t\t${setting.name} -> ${setting.value}")
                    }
            }
    }
}