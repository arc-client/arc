
package com.arc.module.hud

import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.util.Formatting.format
import com.arc.util.ServerTPS
import com.arc.util.ServerTPS.recentData
import imgui.ImVec2

object TPS : HudModule(
	name = "TPS",
	description = "Display the server's tick rate",
	tag = ModuleTag.HUD,
) {
	private val format by setting("Tick format", ServerTPS.TickFormat.Tps)
	private val showGraph by setting("Show TPS Graph", false)
	private val graphHeight by setting("Graph Height", 40f, 10f..200f, 1f)
	private val graphWidth by setting("Graph Width", 200f, 10f..500f, 1f)
	private val graphStride by setting("Graph Stride", 1, 1..20, 1)

	override fun ImGuiBuilder.buildLayout() {
		val data = recentData(format)
		if (data.isEmpty()) {
			text("No ${format.displayName} data yet")
			return
		}
		val current = data.last()
		val avg = data.average().toFloat()
		if (!showGraph) {
			text("${format.displayName}: ${avg.format()}${format.unit}")
			return
		}
		val overlay = "cur ${current.format()}${format.unit} | avg ${avg.format()}${format.unit}"

		plotLines(
			label = "##TPSPlot",
			values = data,
			overlayText = overlay,
			graphSize = ImVec2(graphWidth, graphHeight),
			stride = graphStride
		)
	}
}
