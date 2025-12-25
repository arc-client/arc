
package com.arc.config.groups

import com.arc.config.Configurable
import com.arc.config.SettingGroup
import com.arc.event.events.TickEvent.Companion.ALL_STAGES
import com.arc.interaction.managers.inventory.InventoryConfig
import com.arc.util.NamedEnum
import com.arc.util.item.ItemUtils

class InventorySettings(
    c: Configurable,
    baseGroup: NamedEnum
) : SettingGroup(c), InventoryConfig {
    enum class Group(override val displayName: String) : NamedEnum {
        General("General"),
        Container("Container"),
        Access("Access")
    }

    override val actionsPerSecond by c.setting("Actions Per Second", 100, 0..100, 1, "How many inventory actions can be performed per tick").group(baseGroup, Group.General).index()
    override val tickStageMask by c.setting("Inventory Stage Mask", ALL_STAGES.toSet(), description = "The sub-tick timing at which inventory actions are performed").group(baseGroup, Group.General).index()
    override val disposables by c.setting("Disposables", ItemUtils.defaultDisposables, description = "Items that will be ignored when checking for a free slot").group(baseGroup, Group.Container).index()
    override val swapWithDisposables by c.setting("Swap With Disposables", true, "Swap items with disposable ones").group(baseGroup, Group.Container).index()
    override val providerPriority by c.setting("Provider Priority", InventoryConfig.Priority.WithMinItems, "What container to prefer when retrieving the item from").group(baseGroup, Group.Container).index()
    override val storePriority by c.setting("Store Priority", InventoryConfig.Priority.WithMinItems, "What container to prefer when storing the item to").group(baseGroup, Group.Container).index()

    override val immediateAccessOnly by c.setting("Immediate Access Only", false, "Only allow access to inventories that can be accessed immediately").group(baseGroup, Group.Access).index()
    override val accessShulkerBoxes by c.setting("Access Shulker Boxes", true, "Allow access to the player's shulker boxes").group(baseGroup, Group.Access).index()
    override val accessEnderChest by c.setting("Access Ender Chest", false, "Allow access to the player's ender chest").group(baseGroup, Group.Access).index()
    override val accessChests by c.setting("Access Chests", false, "Allow access to the player's normal chests").group(baseGroup, Group.Access).index()
    override val accessStashes by c.setting("Access Stashes", false, "Allow access to the player's stashes").group(baseGroup, Group.Access).index()
}