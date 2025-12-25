
package com.arc.interaction.managers.rotating

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.managers.LogContext
import com.arc.interaction.managers.LogContext.Companion.LogContextBuilder
import com.arc.interaction.managers.Request
import com.arc.interaction.managers.rotating.Rotation.Companion.dist
import com.arc.threading.runSafe
import com.arc.util.collections.updatableLazy

data class RotationRequest(
    val buildRotation: SafeContext.() -> Rotation?,
    private val automated: Automated,
    var keepTicks: Int = automated.rotationConfig.keepTicks,
    var decayTicks: Int = automated.rotationConfig.decayTicks
) : Request(), LogContext, Automated by automated {
    constructor(
        rotation: Rotation,
        automated: Automated,
        keepTicks: Int = automated.rotationConfig.keepTicks,
        decayTicks: Int = automated.rotationConfig.decayTicks
    ) : this({ rotation }, automated, keepTicks, decayTicks)

    override val requestId = ++requestCount
    override val tickStageMask get() = rotationConfig.tickStageMask

    val rotation = updatableLazy {
        runSafe { buildRotation() }
    }

    var age = 0
    override val nowOrNothing = false

    override val done: Boolean get() {
        return RotationManager.activeRotation.dist(rotation.value ?: return false) <= 0.001
    }

    override fun submit(queueIfMismatchedStage: Boolean): RotationRequest =
        RotationManager.request(this, queueIfMismatchedStage)

    override fun getLogContextBuilder(): LogContextBuilder.() -> Unit = {
        group("Rotation Request") {
            value("Request ID", requestId)
            value("Age", age)
            value("Keep Ticks", keepTicks)
            value("Decay Ticks", decayTicks)
        }
    }

    companion object {
        var requestCount = 0
    }
}