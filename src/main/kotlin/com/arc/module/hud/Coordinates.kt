
package com.arc.module.hud

import com.arc.config.applyEdits
import com.arc.config.groups.FormatterSettings
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.module.HudModule
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafe
import com.arc.util.Formatting.format
import com.arc.util.NamedEnum
import com.arc.util.extension.dimensionName
import com.arc.util.extension.isNether
import com.arc.util.math.Vec2d
import com.arc.util.math.netherCoord
import com.arc.util.math.overworldCoord

object Coordinates : HudModule(
	name = "Coordinates",
	description = "Show your coordinates",
	tag = ModuleTag.HUD,
) {
	enum class Group(override val displayName: String) : NamedEnum {
		CurrentDimension("Current Dimension"),
		OtherDimension("Other Dimension"),
	}

	private val showDimension by setting("Show Dimension", true)

	private val formatter = FormatterSettings(this, Group.CurrentDimension).apply {
		applyEdits {
			::timeFormat.edit { hide() }
		}
	}
//	private val otherFormatter = FormatterSettings(this, Page.OtherDimension).apply {
//		::timeFormat.edit { hide() }
//		::group.edit { defaultValue(FormatterConfig.TupleGrouping.SquareBrackets) }
//	}

	override fun ImGuiBuilder.buildLayout() {
		runSafe {
			val position = player.pos.format(formatter)
			val otherDimensionPos = // ToDo: The system has forced my hand, too bad!. We need to find a way to allow duplicate setting names.
				if (world.isNether) player.overworldCoord.let { Vec2d(it.x, it.z) }.format(formatter.locale, formatter.separator, "[", "]", formatter.precision)
				else player.netherCoord.let { Vec2d(it.x, it.z) }.format(formatter.locale, formatter.separator, "[", "]", formatter.precision)

			val text = "$position $otherDimensionPos"

			val withDimension =
				if (showDimension) "$text ${world.dimensionName}"
				else text

			textCopyable(withDimension)
		}
	}
}
