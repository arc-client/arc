
package com.arc.module

import com.arc.config.settings.complex.Bind
import com.arc.gui.Layout
import com.arc.module.tag.ModuleTag
import java.awt.Color

abstract class HudModule(
    name: String,
    description: String = "",
    tag: ModuleTag,
    val customWindow: Boolean = false,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false,
    defaultKeybind: Bind = Bind.EMPTY,
) : Module(name, description, tag, alwaysListening, enabledByDefault, defaultKeybind), Layout {
    val backgroundColor by setting("Background Color", Color(0, 0, 0, 0))
    val outline by setting("Show Outline", false)
    val outlineWidth by setting("Outline Width", 1f, 0f..10f, 0.1f) { outline }
}
