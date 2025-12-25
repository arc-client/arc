
package com.arc.util

import com.arc.Arc.mc
import com.arc.core.Loadable
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * The [FolderRegister] object is responsible for managing the directory structure of the application.
 */
object FolderRegister : Loadable {
    val minecraft: Path = mc.runDirectory.toPath()
    val arc: Path = minecraft.resolve("arc")
    val config: Path = arc.resolve("config")
    val packetLogs: Path = arc.resolve("packet-log")
    val replay: Path = arc.resolve("replay")
    val cache: Path = arc.resolve("cache")
    val capes: Path = cache.resolve("capes")
    val structure: Path = arc.resolve("structure")
    val maps: Path = arc.resolve("maps")

    val File.relativeMCPath: Path get() = minecraft.relativize(toPath())

    override fun load(): String {
        val folders = listOf(arc, config, packetLogs, replay, cache, capes, structure, maps)
        val createdFolders = folders.mapNotNull {
            if (it.notExists()) {
                it.createDirectories()
            } else null
        }
        return if (createdFolders.isNotEmpty()) {
            "Created directories: ${createdFolders.joinToString { minecraft.parent.relativize(it).toString() }}"
        } else "Loaded ${folders.size} directories"
    }
}
