
package com.arc.config.configurations

import com.arc.config.Configuration
import com.arc.util.FolderRegister
import java.io.File

object ArcConfig : Configuration() {
    override val configName get() = "arc"
    override val primary: File = FolderRegister.config.resolve("$configName.json").toFile()
}
