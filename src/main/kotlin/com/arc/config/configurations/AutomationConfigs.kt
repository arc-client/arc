
package com.arc.config.configurations

import com.arc.config.Configuration
import com.arc.util.FolderRegister
import java.io.File

object AutomationConfigs : Configuration() {
    override val configName = "automation"
    override val primary: File = FolderRegister.config.resolve("${configName}.json").toFile()
}