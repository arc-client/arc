
package com.arc.gui

import com.arc.gui.dsl.ImGuiBuilder

interface Layout {
    fun ImGuiBuilder.buildLayout()
}