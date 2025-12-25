
package com.arc.config

import com.arc.config.configurations.UserAutomationConfigs
import com.arc.config.settings.collections.CollectionSetting.Companion.onDeselect
import com.arc.config.settings.collections.CollectionSetting.Companion.onSelect
import com.arc.module.Module
import com.arc.module.ModuleRegistry.moduleNameMap

class UserAutomationConfig(override val name: String) : AutomationConfig(name, UserAutomationConfigs) {
    val linkedModules = setting<String>("Linked Modules", emptySet(), moduleNameMap.filter { it.value.defaultAutomationConfig != Companion.DEFAULT }.keys)
        .onSelect { name ->
	        moduleNameMap[name]?.let {
		        it.removeLink()
		        it.automationConfig = this@UserAutomationConfig
	        }
        }
	    .onDeselect { name ->
		    moduleNameMap[name]?.let { module ->
			    module.automationConfig = module.defaultAutomationConfig
		    }
	    }

	private fun Module.removeLink() {
		(automationConfig as? UserAutomationConfig)?.linkedModules?.value?.remove(name)
	}
}