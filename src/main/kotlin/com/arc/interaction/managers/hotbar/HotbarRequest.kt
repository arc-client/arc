
package com.arc.interaction.managers.hotbar

import com.arc.context.Automated
import com.arc.interaction.managers.LogContext
import com.arc.interaction.managers.LogContext.Companion.LogContextBuilder
import com.arc.interaction.managers.Request

class HotbarRequest(
    val slot: Int,
    automated: Automated,
    var keepTicks: Int = automated.hotbarConfig.keepTicks,
    var swapPause: Int = automated.hotbarConfig.swapPause,
    override val nowOrNothing: Boolean = true
) : Request(), LogContext, Automated by automated {
    override val requestId = ++requestCount
    override val tickStageMask get() = hotbarConfig.tickStageMask

    var activeRequestAge = 0
    var swapPauseAge = 0

    override val done: Boolean
        get() = slot == HotbarManager.activeSlot && swapPauseAge >= swapPause

    override fun submit(queueIfMismatchedStage: Boolean) =
        HotbarManager.request(this, queueIfMismatchedStage)

    override fun getLogContextBuilder(): LogContextBuilder.() -> Unit = {
        group("Hotbar Request") {
            value("Request ID", requestId)
            value("Slot", slot)
            value("Keep Ticks", keepTicks)
            value("Swap Pause", swapPause)
            value("Swap Pause Age", swapPauseAge)
            value("Active Request Age", activeRequestAge)
        }
    }

    companion object {
        var requestCount = 0
    }
}
