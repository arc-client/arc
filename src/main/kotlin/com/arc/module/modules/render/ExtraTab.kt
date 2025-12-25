
package com.arc.module.modules.render

import com.arc.module.Module
import com.arc.module.tag.ModuleTag

object ExtraTab : Module(
    name = "ExtraTab",
    description = "Adds more tabs to the main menu",
    tag = ModuleTag.RENDER,
) {
    @JvmStatic
    val tabEntries by setting("Tab Entries", 80L, 1L..500L, 1L)

    @JvmStatic
    val rows by setting("Rows", 20, 1..100, 1)
}
