
package com.arc.module.modules.player

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.event.events.InventoryEvent
import com.arc.event.events.PlayerEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.task.RootTask.run
import com.arc.task.Task
import com.arc.task.tasks.BuildTask.Companion.breakAndCollectBlock
import com.arc.task.tasks.OpenContainer
import com.arc.task.tasks.PlaceContainer
import com.arc.util.item.ItemUtils.shulkerBoxes
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.BlockPos

object InventoryTweaks : Module(
    name = "InventoryTweaks",
    tag = ModuleTag.PLAYER,
) {
    private val instantShulker by setting("Instant Shulker", true, description = "Right-click shulker boxes in your inventory to instantly place them and open them.")
    private val instantEChest by setting("Instant Ender-Chest", true, description = "Right-click ender chests in your inventory to instantly place them and open them.")
    private var placedPos: BlockPos? = null
    private var placeAndOpen: Task<*>? = null
    private var lastBreak: Task<*>? = null
    private var lastOpenScreen: ScreenHandler? = null

    init {
        setDefaultAutomationConfig {
            applyEdits {
                hideAllGroupsExcept(breakConfig, interactConfig, inventoryConfig, hotbarConfig)
            }
        }

        listen<PlayerEvent.SlotClick> {
            if (it.action != SlotActionType.PICKUP || it.button != 1) return@listen
            val stack = it.screenHandler.getSlot(it.slot).stack
            if (!(instantShulker && stack.item in shulkerBoxes) && !(instantEChest && stack.item == Items.ENDER_CHEST)) return@listen
            it.cancel()
            lastOpenScreen = null
            placeAndOpen = PlaceContainer(stack, this@InventoryTweaks).then { placePos ->
                placedPos = placePos
                OpenContainer(placePos, this@InventoryTweaks).finally { screenHandler ->
                    lastOpenScreen = screenHandler
                }
            }.run()
        }

        listen<InventoryEvent.Close> { event ->
            if (event.screenHandler != lastOpenScreen) return@listen
            lastOpenScreen = null
            placedPos?.let {
                lastBreak = breakAndCollectBlock(it).run()
                placedPos = null
            }
        }

        onDisable {
            placeAndOpen?.cancel()
            lastBreak?.cancel()
        }
    }
}
