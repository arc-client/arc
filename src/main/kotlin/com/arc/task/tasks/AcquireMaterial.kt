
package com.arc.task.tasks

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.ContainerManager
import com.arc.interaction.material.container.ContainerManager.findContainerWithMaterial
import com.arc.task.Task

class AcquireMaterial @Ta5kBuilder constructor(
    val selection: StackSelection,
    automated: Automated
) : Task<StackSelection>(), Automated by automated {
    override val name: String
        get() = "Acquiring $selection"

    override fun SafeContext.onStart() {
        selection.findContainerWithMaterial()
            ?.withdraw(selection)
            ?.finally {
                success(selection)
            }?.execute(this@AcquireMaterial)
            ?: failure(ContainerManager.NoContainerFound(selection)) // ToDo: Create crafting path
    }

    companion object {
        @Ta5kBuilder
        fun Automated.acquire(selection: () -> StackSelection) =
            AcquireMaterial(selection(), this)
    }
}
