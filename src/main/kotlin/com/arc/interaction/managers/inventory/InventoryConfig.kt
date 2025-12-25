
package com.arc.interaction.managers.inventory

import com.arc.config.ISettingGroup
import com.arc.event.events.TickEvent
import com.arc.interaction.material.ContainerSelection
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.util.Describable
import com.arc.util.NamedEnum
import net.minecraft.block.Block

interface InventoryConfig : ISettingGroup {
    val actionsPerSecond: Int
    val tickStageMask: Collection<TickEvent>
    val disposables: Collection<Block>
    val swapWithDisposables: Boolean
    val providerPriority: Priority
    val storePriority: Priority

    val immediateAccessOnly: Boolean
    val accessShulkerBoxes: Boolean
    val accessEnderChest: Boolean
    val accessChests: Boolean
    val accessStashes: Boolean

    val containerSelection: ContainerSelection
        get() = ContainerSelection.selectContainer {
            val allowedContainers = mutableSetOf<MaterialContainer.Rank>().apply {
                addAll(MaterialContainer.Rank.entries)
                if (!accessShulkerBoxes) remove(MaterialContainer.Rank.ShulkerBox)
                if (!accessEnderChest) remove(MaterialContainer.Rank.EnderChest)
                if (!accessChests) remove(MaterialContainer.Rank.Chest)
                if (!accessStashes) remove(MaterialContainer.Rank.Stash)
            }
            ofAnyType(*allowedContainers.toTypedArray())
        }

    enum class Priority(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        WithMinItems("With Min Items", "Pick containers with the fewest matching items (or least space) first; useful for topping off or clearing leftovers."),
        WithMaxItems("With Max Items", "Pick containers with the most matching items (or most space) first; ideal for bulk moves with fewer transfers.");

        fun materialComparator(selection: StackSelection) =
            when (this) {
                WithMaxItems -> compareBy<MaterialContainer> { it.rank }
                    .thenByDescending { it.materialAvailable(selection) }
                    .thenBy { it.name }

                WithMinItems -> compareBy<MaterialContainer> { it.rank }
                    .thenBy { it.materialAvailable(selection) }
                    .thenBy { it.name }
            }

        fun spaceComparator(selection: StackSelection) =
            when (this) {
                WithMaxItems -> compareBy<MaterialContainer> { it.rank }
                    .thenByDescending { it.spaceAvailable(selection) }
                    .thenBy { it.name }

                WithMinItems -> compareBy<MaterialContainer> { it.rank }
                    .thenBy { it.spaceAvailable(selection) }
                    .thenBy { it.name }
            }
    }
}
