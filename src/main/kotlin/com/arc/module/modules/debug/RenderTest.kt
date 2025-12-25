
package com.arc.module.modules.debug

import com.arc.event.events.onDynamicRender
import com.arc.event.events.onStaticRender
import com.arc.graphics.renderer.esp.DirectionMask
import com.arc.graphics.renderer.esp.DynamicAABB.Companion.dynamicBox
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.math.setAlpha
import com.arc.util.world.entitySearch
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import java.awt.Color

object RenderTest : Module(
    name = "Render:shrimp:Test:canned_food:",
    description = "RenderTest",
    tag = ModuleTag.DEBUG,
) {
    private val test1 by setting("Toggle visibility", true)
    private val test21 by setting("Hallo 1", true, visibility = ::test1)
    private val test22 by setting("Hallo Slider", 1.0, 0.0..5.0, 0.5, visibility = ::test1)
    private val test23 by setting("Hallo String", "bruh", visibility = ::test1)
    private val test31 by setting("Holla huh 1", true, visibility = { !test1 })
    private val test32 by setting("Holla buh 2", true, visibility = { !test1 })

    private val outlineColor = Color(100, 150, 255).setAlpha(0.5)
    private val filledColor = outlineColor.setAlpha(0.2)

    init {
        onDynamicRender {
            entitySearch<LivingEntity>(8.0)
                .forEach { entity ->
                    it.box(entity.dynamicBox, filledColor, outlineColor, DirectionMask.ALL, DirectionMask.OutlineMode.And)
                }
        }

        onStaticRender {
            it.box(Box.of(player.pos, 0.3, 0.3, 0.3), filledColor, outlineColor)
        }
    }
}
