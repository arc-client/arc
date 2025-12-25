
package com.arc.module.hud

import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.ModuleRegistry
import com.arc.module.tag.ModuleTag
import imgui.flag.ImGuiCol
import java.awt.Color

object ModuleList : HudModule(
    name = "ModuleList",
    tag = ModuleTag.HUD,
) {
	val onlyBound by setting("Only Bound", false, "Only displays modules with a keybind")
	val showKeybind by setting("Show Keybind", true, "Display keybind next to a module")

    init {
        drawSetting.value = false
    }

    override fun ImGuiBuilder.buildLayout() {
        val enabled = ModuleRegistry.modules.filter { it.isEnabled && it.draw }

        enabled.forEach {
            val bound = it.keybind.key != 0 || it.keybind.mouse != -1
            if (onlyBound && !bound) return@forEach
            text(it.name);

	        if (showKeybind) {
		        val color = if (!bound) Color.RED else Color.GREEN

		        sameLine()
		        withStyleColor(ImGuiCol.Text, color) { text(" [${it.keybind.name}]") }
	        }
        }
    }
}
