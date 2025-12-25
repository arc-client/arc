
package com.arc.module.hud

import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafe

object AccountName : HudModule(
    name = "AccountName",
    description = "Displays the current accounts name",
    tag = ModuleTag.HUD
) {
    override fun ImGuiBuilder.buildLayout() {
        runSafe { text(player.name.string) }
    }
}