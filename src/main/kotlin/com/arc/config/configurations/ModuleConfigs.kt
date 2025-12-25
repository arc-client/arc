
package com.arc.config.configurations

import com.arc.config.Configuration
import com.arc.config.configurations.ModuleConfigs.configName
import com.arc.config.configurations.ModuleConfigs.primary
import com.arc.util.FolderRegister
import java.io.File


/**
 * The [ModuleConfigs] object represents the configuration file for the [Module]s.
 *
 * This object is used to save and load the settings of all [Module]s in the system.
 *
 * @property configName The name of the configuration.
 * @property primary The primary file where the configuration is saved.
 */
object ModuleConfigs : Configuration() {
    override val configName get() = "modules"
    override val primary: File = FolderRegister.config.resolve("$configName.json").toFile()
}
