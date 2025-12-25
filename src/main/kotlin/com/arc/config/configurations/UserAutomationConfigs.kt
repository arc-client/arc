
package com.arc.config.configurations

import com.google.gson.JsonParser
import com.arc.config.Configuration
import com.arc.config.UserAutomationConfig
import com.arc.module.ModuleRegistry.moduleNameMap
import com.arc.util.FileUtils.ifExists
import com.arc.util.FolderRegister
import java.io.File

object UserAutomationConfigs : Configuration() {
    override val configName = "custom-automation"
    override val primary: File = FolderRegister.config.resolve("${configName}.json").toFile()

    override fun internalTryLoad() {
        primary.ifExists {
            JsonParser.parseReader(it.reader()).asJsonObject.entrySet().forEach { (name, _) ->
                if (configurables.any { config -> config.name == name }) return@forEach
                UserAutomationConfig(name)
            }
        }
        super.internalTryLoad()
        configurables.forEach {
            val config = it as? UserAutomationConfig ?: throw IllegalStateException("UserAutomationConfigs contains non-UserAutomationConfig")
            config.linkedModules.value.forEach { moduleName ->
                moduleNameMap[moduleName]?.automationConfig = config
            }
        }
    }
}