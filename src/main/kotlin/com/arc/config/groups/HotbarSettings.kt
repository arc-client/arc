
package com.arc.config.groups

import com.arc.config.Configurable
import com.arc.config.SettingGroup
import com.arc.event.events.TickEvent
import com.arc.event.events.TickEvent.Companion.ALL_STAGES
import com.arc.interaction.managers.hotbar.HotbarConfig
import com.arc.util.NamedEnum

class HotbarSettings(
    c: Configurable,
    baseGroup: NamedEnum
) : SettingGroup(c), HotbarConfig {
    override val swapMode by c.setting("Swap Mode", HotbarConfig.SwapMode.Temporary).group(baseGroup).index()
    override val keepTicks by c.setting("Keep Ticks", 1, 0..20, 1, "The number of ticks to keep the current hotbar selection active", " ticks") { swapMode == HotbarConfig.SwapMode.Temporary }.group(baseGroup).index()
    override val swapDelay by c.setting("Swap Delay", 0, 0..3, 1, "The number of ticks delay before allowing another hotbar selection swap", " ticks").group(baseGroup).index()
    override val swapsPerTick by c.setting("Swaps Per Tick", 3, 1..10, 1, "The number of hotbar selection swaps that can take place each tick") { swapDelay <= 0 }.group(baseGroup).index()
    override val swapPause by c.setting("Swap Pause", 0, 0..20, 1, "The delay in ticks to pause actions after switching to the slot", " ticks").group(baseGroup).index()
    override val tickStageMask by c.setting("Hotbar Stage Mask", setOf(TickEvent.Input.Post), ALL_STAGES.toSet(), description = "The sub-tick timing at which hotbar actions are performed").group(baseGroup).index()
}