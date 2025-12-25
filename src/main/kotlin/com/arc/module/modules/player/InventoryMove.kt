
package com.arc.module.modules.player

import com.arc.Arc.mc
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.gui.ArcScreen
import com.arc.interaction.managers.Request.Companion.submit
import com.arc.interaction.managers.rotating.Rotation
import com.arc.interaction.managers.rotating.RotationConfig
import com.arc.interaction.managers.rotating.RotationMode
import com.arc.interaction.managers.rotating.RotationRequest
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.InputUtils.isKeyPressed
import com.arc.util.math.MathUtils.toFloatSign
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen
import net.minecraft.client.gui.screen.ingame.AnvilScreen
import net.minecraft.client.gui.screen.ingame.BookEditScreen
import org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_2
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_4
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_6
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_8
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import org.lwjgl.glfw.GLFW.GLFW_KEY_UP

object InventoryMove : Module(
    name = "InventoryMove",
    description = "Allows you to move with GUIs opened",
    tag = ModuleTag.PLAYER,
) {
    private val clickGui by setting("ClickGui", false)
    private val disableSneak by setting("Disable Crouch", false)
    private val arrowKeys by setting("Arrow Keys", false, "Allows rotating the players camera using the arrow keys")
    private val speed by setting("Rotation Speed", 5, 1..20, 1, unit = "°/tick") { arrowKeys }
    override val rotationConfig = RotationConfig.Instant(RotationMode.Lock)

    @JvmStatic
    val shouldMove get() = isEnabled && !mc.currentScreen.hasInputOrNull

    /**
     * Whether the current screen has text inputs or is null
     */
    @JvmStatic
    val Screen?.hasInputOrNull: Boolean
        get() = this is ChatScreen ||
                this is AbstractSignEditScreen ||
                this is AnvilScreen ||
                this is AbstractCommandBlockScreen ||
                (this is ArcScreen && !clickGui) ||
                this is BookEditScreen ||
                this == null

    init {
        listen<TickEvent.Pre> {
            if (!arrowKeys || mc.currentScreen.hasInputOrNull) return@listen

            val pitch = (isKeyPressed(GLFW_KEY_DOWN, GLFW_KEY_KP_2).toFloatSign() -
                    isKeyPressed(GLFW_KEY_UP, GLFW_KEY_KP_8).toFloatSign()) * speed
            val yaw = (isKeyPressed(GLFW_KEY_RIGHT, GLFW_KEY_KP_6).toFloatSign() -
                    isKeyPressed(GLFW_KEY_LEFT, GLFW_KEY_KP_4).toFloatSign()) * speed

            submit(RotationRequest(Rotation(player.yaw + yaw, (player.pitch + pitch).coerceIn(-90f, 90f)), this@InventoryMove))
        }
    }

    @JvmStatic
    fun isKeyMovementRelated(key: Int): Boolean {
        val options = mc.options
        return when (key) {
            options.forwardKey.boundKey.code,
            options.backKey.boundKey.code,
            options.leftKey.boundKey.code,
            options.rightKey.boundKey.code,
            options.jumpKey.boundKey.code,
            options.sprintKey.boundKey.code -> true
            options.sneakKey.boundKey.code if (!disableSneak) -> true
            else -> false
        }
    }
}
