
package com.arc.core

import com.arc.Arc
import com.arc.Arc.LOG
import com.arc.util.Communication.ascii
import com.arc.util.reflections.getInstances
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

object Loader {
    private val started = System.currentTimeMillis()

    val runtime: String
        get() = "${(System.currentTimeMillis() - started).milliseconds}"

    private val loadables = getInstances<Loadable>()

    fun initialize(): Long {
        ascii.split("\n").forEach { LOG.info(it) }
        LOG.info("Initializing ${Arc.MOD_NAME} ${Arc.VERSION} (${loadables.size} loaders)...")

        val initTime = measureTimeMillis {
            loadables.sortedByDescending { it.priority }.forEach {
                var response: String
                val time = measureTimeMillis { response = it.load() }
                if (response.isNotBlank()) LOG.info("$response ($time ms)")
            }
        }

        return initTime
    }
}
