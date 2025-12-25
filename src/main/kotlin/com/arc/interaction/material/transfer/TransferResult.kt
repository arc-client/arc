
package com.arc.interaction.material.transfer

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.interaction.material.StackSelection
import com.arc.interaction.material.container.MaterialContainer
import com.arc.task.Task

abstract class TransferResult : Task<Unit>() {
    data class ContainerTransfer(
        val selection: StackSelection,
        val from: MaterialContainer,
        val to: MaterialContainer,
        val automated: Automated
    ) : TransferResult(), Automated by automated {
        override val name = "Container Transfer of [$selection] from [${from.name}] to [${to.name}]"

        override fun SafeContext.onStart() {
            val withdrawal = from.withdraw(selection)
            val deposit = to.deposit(selection)

            val task = when {
                withdrawal != null && deposit != null -> {
                    withdrawal.then {
                        deposit.finally { success() }
                    }
                }
                withdrawal != null -> {
                    withdrawal.finally { success() }
                }
                deposit != null -> {
                    deposit.finally { success() }
                }
                else -> null
            }

            task?.execute(this@ContainerTransfer)
        }
    }

    data object NoSpace : TransferResult() {
        override val name = "No space left in the target container"

        // ToDo: Needs inventory space resolver. compressing or disposing
        override fun SafeContext.onStart() {
            failure("No space left in the target container")
        }
    }

    data class MissingItems(val missing: Int) : TransferResult() {
        override val name = "Missing $missing items"

        // ToDo: Find other satisfying permutations
        override fun SafeContext.onStart() {
            failure("Missing $missing items")
        }
    }
}
