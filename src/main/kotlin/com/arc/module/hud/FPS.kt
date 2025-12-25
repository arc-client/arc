
package com.arc.module.hud

import com.arc.event.events.RenderEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.util.collections.LimitedDecayQueue
import kotlin.time.Duration.Companion.seconds

object FPS : HudModule(
	name = "FPS",
	description = "Displays your games frames per second",
	tag = ModuleTag.HUD
) {
	val average by setting("Average", true)
	val updateDelay by setting("Update Delay", 50, 0..1000, 1, "Time between updating the fps value")

	val frames = LimitedDecayQueue<Unit>(Int.MAX_VALUE, 1.seconds.inWholeMilliseconds)
	var lastUpdated = System.currentTimeMillis()
	var lastFrameTime = System.nanoTime()
	var fps = 0

	init {
		listen<RenderEvent.Render> {
			var currentFps = 0
			if (average) {
				frames.add(Unit)
				currentFps = frames.size
			} else {
				val currentTimeNano = System.nanoTime()
				val elapsedNs = currentTimeNano - lastFrameTime
				currentFps = if (elapsedNs > 0) (1000000000 / elapsedNs).toInt() else 0
				lastFrameTime = currentTimeNano
			}

			val currentTypeMilli = System.currentTimeMillis()
			if (currentTypeMilli - lastUpdated >= updateDelay) {
				fps = currentFps
				lastUpdated = currentTypeMilli
			}
		}
	}

	override fun ImGuiBuilder.buildLayout() {
		text("FPS: $fps")
	}
}