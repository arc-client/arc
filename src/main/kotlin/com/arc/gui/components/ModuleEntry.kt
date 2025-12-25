
package com.arc.gui.components

import com.arc.gui.Layout
import com.arc.gui.components.SettingsWidget.buildConfigSettingsContext
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.Module
import imgui.ImGui

class ModuleEntry(val module: Module): Layout {
    override fun ImGuiBuilder.buildLayout() {
        selectable(module.name, selected = module.isEnabled) {
            module.toggle()
        }
        arcTooltip(module.description)

        ImGui.setNextWindowSizeConstraints(0f, 0f, Float.MAX_VALUE, io.displaySize.y * 0.5f)
        popupContextItem("##ctx-${module.name}") {
            buildConfigSettingsContext(module)
        }
    }
}