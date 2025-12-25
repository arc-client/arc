
package com.arc.interaction.construction.simulation.result

import com.arc.context.Automated
import com.arc.context.SafeContext
import com.arc.task.Task

/**
 * Represents a [BuildResult] with a resolvable [Task]
 */
interface Resolvable {
    context(automated: Automated, safeContext: SafeContext)
    fun resolve(): Task<*>?
}
