
package com.arc.util.player

import com.arc.context.SafeContext
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType

object SlotUtils {
    val ClientPlayerEntity.hotbar: List<ItemStack> get() = inventory.mainStacks.slice(0..8)
    val ClientPlayerEntity.storage: List<ItemStack> get() = inventory.mainStacks.slice(9..35)
    val ClientPlayerEntity.equipment: List<ItemStack> get() = inventory.equipment.map.map { it.value }
    val ClientPlayerEntity.hotbarAndStorage: List<ItemStack> get() = inventory.mainStacks
    val ClientPlayerEntity.combined: List<ItemStack> get() = hotbarAndStorage + equipment

    fun SafeContext.clickSlot(slotId: Int, button: Int, actionType: SlotActionType) {
        val syncId = player.currentScreenHandler?.syncId ?: return
        interaction.clickSlot(syncId, slotId, button, actionType, player)
    }
}
