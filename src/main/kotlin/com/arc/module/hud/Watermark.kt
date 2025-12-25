
package com.arc.module.hud

import com.arc.graphics.texture.TextureOwner.upload
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import imgui.ImGui

object Watermark : HudModule(
    name = "Watermark",
    tag = ModuleTag.HUD,
    enabledByDefault = true,
) {
    private val texture = upload("textures/arc.png")
    private val scale by setting("Scale", 0.15f, 0.01f..1f, 0.01f)

    override fun ImGuiBuilder.buildLayout() {
        val width = texture.width * scale
        val height = texture.height * scale
        ImGui.image(texture.id.toLong(), width, height)
    }
}
