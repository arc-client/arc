
package com.arc.graphics.renderer.gui.font.core

import com.arc.graphics.renderer.gui.font.core.ArcAtlas.buildBuffer

enum class ArcFont(val fontName: String) {
    FiraSansRegular("FiraSans-Regular"),
    FiraSansBold("FiraSans-Bold");

    fun load(): String {
        entries.forEach { it.buildBuffer() }
        return "Loaded ${entries.size} fonts"
    }
}
