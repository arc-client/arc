
package com.arc.module.modules.debug

import com.arc.event.events.KeyboardEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.BlockUtils.blockState
import com.arc.util.Communication.info
import com.arc.util.KeyCode
import net.minecraft.block.BlockState
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.hit.BlockHitResult

object StateInfo : Module(
    name = "StateInfo",
    description = "Prints the target block's state into chat",
    tag = ModuleTag.DEBUG,
) {
    private val printBind by setting("Print", KeyCode.Unbound, "The bind used to print the info to chat")

    val propertyFields = Properties::class.java.declaredFields
        .filter { Property::class.java.isAssignableFrom(it.type) }
        .associateBy { it.get(null) as Property<*> }

    init {
        onEnable {
            val crosshair = mc.crosshairTarget ?: return@onEnable
            if (crosshair !is BlockHitResult) return@onEnable
            info(blockState(crosshair.blockPos).betterToString())
        }

        listen<KeyboardEvent.Press> { event ->
            if (!event.isPressed ||
                !event.satisfies(printBind)) return@listen

            val crosshair = mc.crosshairTarget ?: return@listen
            if (crosshair !is BlockHitResult) return@listen
            info(blockState(crosshair.blockPos).betterToString())
        }
    }

    private fun BlockState.betterToString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(this.owner.toString() + "\n")

        if (entries.isNotEmpty()) {
            stringBuilder.append("      [\n")

            stringBuilder.append(
                entries.entries.joinToString("\n") { (property, value) ->
                    val fieldName = propertyFields[property]?.name ?: property.toString()
                    "          $fieldName = ${nameValue(property, value)}"
                }
            )

            stringBuilder.append("\n      ]")
        }

        return stringBuilder.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>?> nameValue(property: Property<T>, value: Comparable<*>): String {
        return property.name(value as T)
    }
}
