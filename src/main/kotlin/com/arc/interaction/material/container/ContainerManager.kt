
package com.arc.interaction.material.container

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.core.Loadable
import com.arc.event.events.InventoryEvent
import com.arc.event.events.PlayerEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.material.ContainerSelection
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.StackSelection.Companion.select
import com.arc.interaction.material.container.containers.ChestContainer
import com.arc.interaction.material.container.containers.EnderChestContainer
import com.arc.util.BlockUtils.blockEntity
import com.arc.util.extension.containerStacks
import com.arc.util.reflections.getInstances
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.EnderChestBlockEntity
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType

// ToDo: Make this a Configurable to save container caches. Should use a cached region based storage system.
object ContainerManager : Loadable {
    private val containers: List<MaterialContainer>
        get() = compileContainers + runtimeContainers

    private val compileContainers = getInstances<MaterialContainer>()
    private val runtimeContainers = mutableSetOf<MaterialContainer>()

    private var lastInteractedBlockEntity: BlockEntity? = null

    override fun load() = "Loaded ${compileContainers.size} containers"

    init {
        listen<PlayerEvent.Interact.Block> {
            lastInteractedBlockEntity = blockEntity(it.blockHitResult.blockPos)
        }

        listen<InventoryEvent.Close> { event ->
            if (event.screenHandler !is GenericContainerScreenHandler) return@listen

            val handler = event.screenHandler

            when (val block = lastInteractedBlockEntity) {
                is EnderChestBlockEntity -> {
                    if (handler.type != ScreenHandlerType.GENERIC_9X3) return@listen

                    EnderChestContainer.update(handler.containerStacks)
                }

                is ChestBlockEntity -> {
                    // ToDo: Handle double chests and single chests
                    if (handler.type != ScreenHandlerType.GENERIC_9X6) return@listen
                    val stacks = handler.containerStacks

                    containers
                        .filterIsInstance<ChestContainer>()
                        .find {
                            it.blockPos == block.pos
                        }?.update(stacks) ?: runtimeContainers.add(ChestContainer(stacks, block.pos))
                }
            }
            lastInteractedBlockEntity = null
        }
    }

    fun containers() = containers.flatMap { setOf(it) + it.shulkerContainer }.sorted()

    context(automated: Automated, _: SafeContext)
    fun StackSelection.transfer(destination: MaterialContainer) =
        findContainerWithMaterial()?.transfer(this, destination)

    fun findContainer(
        block: (MaterialContainer) -> Boolean,
    ): MaterialContainer? = containers().find(block)

    context(_: Automated, _: SafeContext)
    fun StackSelection.findContainerWithMaterial(): MaterialContainer? =
        findContainersWithMaterial().firstOrNull()

    context(_: Automated, _: SafeContext)
    fun findContainerWithSpace(selection: StackSelection): MaterialContainer? =
        findContainersWithSpace(selection).firstOrNull()

    context(automated: Automated, safeContext: SafeContext)
    fun StackSelection.findContainersWithMaterial(
        containerSelection: ContainerSelection = automated.inventoryConfig.containerSelection,
    ): List<MaterialContainer> =
        containers()
            .filter { !automated.inventoryConfig.immediateAccessOnly || it.isImmediatelyAccessible() }
            .filter { it.materialAvailable(this) >= count }
            .filter { containerSelection.matches(it) }
            .sortedWith(automated.inventoryConfig.providerPriority.materialComparator(this))

    context(automated: Automated, safeContext: SafeContext)
    fun findContainersWithSpace(
        selection: StackSelection,
    ): List<MaterialContainer> =
        containers()
            .filter { !automated.inventoryConfig.immediateAccessOnly || it.isImmediatelyAccessible() }
            .filter { it.spaceAvailable(selection) >= selection.count }
            .filter { automated.inventoryConfig.containerSelection.matches(it) }
            .sortedWith(automated.inventoryConfig.providerPriority.spaceComparator(selection))

    context(automated: Automated)
    fun findDisposable() = containers().find { container ->
        automated.inventoryConfig.disposables.any { container.materialAvailable(it.asItem().select()) > 0 }
    }

    class NoContainerFound(selection: StackSelection) : Exception("No container found matching $selection")
}
