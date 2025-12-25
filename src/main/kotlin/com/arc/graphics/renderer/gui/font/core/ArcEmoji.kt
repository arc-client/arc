
package com.arc.graphics.renderer.gui.font.core

import com.arc.graphics.renderer.gui.font.core.ArcAtlas.buildBuffer

enum class ArcEmoji(val url: String) {
    Twemoji("fonts/emojis.zip");

    private val emojiRegex = Regex(":[a-zA-Z0-9_]+:")

    /**
     * Extracts emoji names from the provided text
     *
     * The function scans the input text for patterns matching emojis in the `:name:` format and
     * returns a mutable list of the emoji names
     *
     * @param text The text to parse.
     * @return A list of extract emoji names
     */
    fun parse(text: String): MutableList<String> =
        emojiRegex.findAll(text).map { it.value }.toMutableList()

    fun load(): String {
        entries.forEach { it.buildBuffer() }
        return "Loaded ${entries.size} emoji sets"
    }
}
