
package com.arc.task.tasks

import com.arc.config.groups.EatConfig
import com.arc.config.groups.EatConfig.Companion.reasonEating
import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.material.container.ContainerManager.transfer
import com.arc.interaction.material.container.containers.MainHandContainer
import com.arc.task.Task
import com.arc.threading.runSafeAutomated
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

class EatTask @Ta5kBuilder constructor(
    automated: Automated
) : Task<Unit>(), Automated by automated {
    override val name: String
        get() = reason.message(eatStack ?: ItemStack.EMPTY)

    private var eatStack: ItemStack? = null
    private var reason = EatConfig.Reason.None
    private var holdingUse = false

    override fun SafeContext.onStart() {
        reason = runSafeAutomated { reasonEating() }
    }

    init {
        listen<TickEvent.Input.Pre> {
            if (holdingUse && !reason.shouldKeepEating(eatStack)) {
                mc.options.useKey.isPressed = false
                holdingUse = false
                interaction.stopUsingItem(player)
                success()
                return@listen
            }

            if (player.isUsingItem) {
                if (!holdingUse) {
                    mc.options.useKey.isPressed = true
                    holdingUse = true
                }
                return@listen
            }

            val foodFinder = reason.selector()
            if (!foodFinder.matches(player.mainHandStack)) {
                if (holdingUse) {
                    mc.options.useKey.isPressed = false
                    holdingUse = false
                }
                foodFinder.transfer(MainHandContainer)
                    ?.execute(this@EatTask) ?: failure("No food found")
                return@listen
            }
            eatStack = player.mainHandStack

            (interaction.interactItem(player, Hand.MAIN_HAND) as? ActionResult.Success)?.let {
                if (it.swingSource == ActionResult.SwingSource.CLIENT) player.swingHand(Hand.MAIN_HAND)
                mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND)
                mc.options.useKey.isPressed = true
                holdingUse = true
            }
        }
    }

    companion object {
        @Ta5kBuilder
        context(automated: Automated)
        fun eat() = EatTask(automated)
    }
}
