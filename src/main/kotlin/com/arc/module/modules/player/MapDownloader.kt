
package com.arc.module.modules.player

import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.FileUtils.locationBoundDirectory
import com.arc.util.FolderRegister
import com.arc.util.StringUtils.hashString
import com.arc.util.player.SlotUtils.combined
import com.arc.util.world.entitySearch
import net.minecraft.block.MapColor
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.item.FilledMapItem
import net.minecraft.item.map.MapState
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object MapDownloader : Module(
    name = "MapDownloader",
    description = "Save map data to your computer",
    tag = ModuleTag.PLAYER,
) {
    init {
        listen<TickEvent.Pre> {
            val mapStates = entitySearch<ItemFrameEntity>(128.0)
                .mapNotNull { FilledMapItem.getMapState(it.heldItemStack, world) } +
                    player.combined.mapNotNull { FilledMapItem.getMapState(it, world) }

            mapStates.forEach { map ->
                val name = map.hash
                val image = map.toBufferedImage()

                val file = FolderRegister.maps.toFile().locationBoundDirectory().resolve("$name.png")
                if (file.exists()) return@listen

                ImageIO.write(image, "png", file)
            }
        }
    }

    private val MapState.hash: String
        get() = colors.hashString("SHA-256")

    fun MapState.toBufferedImage(): BufferedImage {
        val image = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)

        repeat(128) { x ->
            repeat(128) { y ->
                val index = colors[x + y * 128].toInt()
                val color = MapColor.getRenderColor(index)

                val b = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val r = (color shr 0) and 0xFF

                val argb = -0x1000000 or (r shl 16) or (g shl 8) or (b shl 0)
                image.setRGB(x, y, argb)
            }
        }

        return image
    }
}
