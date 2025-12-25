
package com.arc.module.modules.render

import com.arc.graphics.renderer.gui.font.core.ArcEmoji
import com.arc.graphics.renderer.gui.font.core.ArcFont
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.NamedEnum
import java.awt.Color

object StyleEditor : Module(
    name = "StyleEditor",
    description = "Modify the style of the GUI",
    tag = ModuleTag.RENDER,
) {
    val alpha by setting("Alpha", 1.0, 0.2..1.0, 0.005, "Global alpha applies to everything in Dear ImGui")
    val disabledAlpha by setting("Disabled Alpha", 0.6, 0.0..1.0, 0.005, "Additional alpha multiplier applied by BeginDisabled().  Multiply over current value of Alpha")
    //val windowPaddingH by setting("Horizontal Window Padding", 8, 0)
    val itemSpacing by setting("Item Spacing", 21.0, 0.0..30.0, 1.0, "Horizontal spacing when e.g. entering a tree node") // Generally == (FontSize + FramePadding.x*2)
    val scrollbarSize by setting("Scrollbar Size", 14.0, 1.0..20.0, 1.0)

    // General
    val useMemoryMapping by setting("Use Memory Mapping", true).group(Group.General)

    // Font
    val textFont by setting("Text Font", ArcFont.FiraSansRegular).group(Group.Font)
    val emojiFont by setting("Emoji Font", ArcEmoji.Twemoji).group(Group.Font)
    val shadow by setting("Shadow", true).group(Group.Font)
    val shadowBrightness by setting("Shadow Brightness", 0.35, 0.0..0.5, 0.01) { shadow }.group(Group.Font)
    val shadowShift by setting("Shadow Shift", 1.0, 0.0..2.0, 0.05) { shadow }.group(Group.Font)
    val gap by setting("Gap", 1.5, -10.0..10.0, 0.5).group(Group.Font)
    val baselineOffset by setting("Vertical Offset", 0.0, -10.0..10.0, 0.5).group(Group.Font)
    val highlightColor by setting("Text Highlight Color", Color(214, 55, 87)).group(Group.Font)
    val sdfMin by setting("SDF Min", 0.4, 0.0..1.0, 0.01).group(Group.Font)
    val sdfMax by setting("SDF Max", 1.0, 0.0..1.0, 0.01).group(Group.Font)

    // ESP
    val uploadsPerTick by setting("Uploads", 16, 1..256, 1, unit = " chunks/tick").group(Group.ESP)
    val rebuildsPerTick by setting("Rebuilds", 64, 1..256, 1, unit = " chunks/tick").group(Group.ESP)
    val updateFrequency by setting("Update Frequency", 2, 1..10, 1, "Frequency of block updates", unit = " ticks").group(Group.ESP)
    val outlineWidth by setting("Outline Width", 1.0, 0.1..5.0, 0.1, "Width of block outlines", unit = "px").group(Group.ESP)

    private enum class Group(override val displayName: String): NamedEnum {
        General("General"),
        Font("Font"),
        ESP("ESP"),
    }
}
