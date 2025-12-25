
package com.arc.gui

import com.arc.gui.components.ClickGuiLayout
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ArcScreen : Screen(Text.of("Arc")) {
    override fun shouldPause() = false
    override fun removed() = ClickGuiLayout.close()
    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {}
}
