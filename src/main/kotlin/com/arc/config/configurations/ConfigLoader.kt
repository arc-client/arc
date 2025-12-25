
package com.arc.config.configurations

import com.arc.config.Configuration
import com.arc.core.Loadable

object ConfigLoader: Loadable {
    override val priority = 0
    override fun load(): String {
        Configuration.configurations.forEach {
            it.tryLoad()
        }
        return "Loading ${Configuration.configurations.size} configurations"
    }
}