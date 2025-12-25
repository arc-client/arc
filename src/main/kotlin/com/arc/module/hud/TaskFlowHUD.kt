
package com.arc.module.hud

import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask

object TaskFlowHUD : HudModule(
    name = "TaskFlowHud",
    tag = ModuleTag.HUD,
) {
    override fun ImGuiBuilder.buildLayout() {
        text(RootTask.toString())
    }
}
