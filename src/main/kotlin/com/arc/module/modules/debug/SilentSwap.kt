
package com.arc.module.modules.debug

import com.arc.config.groups.HotbarSettings
import com.arc.event.events.PlayerEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.Request.Companion.submit
import com.arc.interaction.managers.hotbar.HotbarRequest
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.Communication.info
import com.arc.util.NamedEnum

object SilentSwap : Module(
    name = "SilentSwap",
    description = "SilentSwap",
    tag = ModuleTag.DEBUG,
) {
    private enum class Group(override val displayName: String) : NamedEnum {
        Hotbar("Hotbar")
    }

    override val hotbarConfig = HotbarSettings(this, Group.Hotbar)

    init {
        listen<PlayerEvent.Attack.Block> {
            if (!submit(HotbarRequest(0, this@SilentSwap)).done) {
                it.cancel()
                return@listen
            }
            info("${interaction.lastSelectedSlot} ${player.mainHandStack}")
        }
    }
}
