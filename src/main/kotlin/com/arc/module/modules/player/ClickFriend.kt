
package com.arc.module.modules.player

import com.arc.config.settings.complex.Bind
import com.arc.event.events.MouseEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.friend.FriendManager
import com.arc.friend.FriendManager.befriend
import com.arc.friend.FriendManager.isFriend
import com.arc.friend.FriendManager.unfriend
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.Communication.info
import com.arc.util.world.raycast.RayCastUtils.entityResult
import net.minecraft.client.network.OtherClientPlayerEntity
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT

object ClickFriend : Module(
    name = "ClickFriend",
    description = "Add or remove friends with a single click",
    tag = ModuleTag.PLAYER,
) {
    private val friendBind by setting("Friend Bind", Bind(0, 0, GLFW.GLFW_MOUSE_BUTTON_MIDDLE), "Bind to press to befriend a player")
    private val unfriendBind by setting("Unfriend Bind", Bind(0, GLFW_MOD_SHIFT, GLFW.GLFW_MOUSE_BUTTON_MIDDLE), "Bind to press to unfriend a player")

    init {
        listen<MouseEvent.Click> {
            if (mc.currentScreen != null) return@listen

            val target = mc.crosshairTarget?.entityResult?.entity as? OtherClientPlayerEntity
                ?: return@listen

            when {
                it.satisfies(friendBind) && !target.isFriend && target.befriend() ->
                    info(FriendManager.befriendedText(target.name))

                it.satisfies(unfriendBind) && target.isFriend && target.unfriend() ->
                    info(FriendManager.unfriendedText(target.name))
            }
        }
    }
}
