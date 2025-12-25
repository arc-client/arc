
package com.arc.util

import com.arc.Arc
import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

typealias ArcResource = String

val ArcResource.stream: InputStream
    get() = Arc::class.java.getResourceAsStream("/assets/arc/$this")
        ?: throw FileNotFoundException("File \"/assets/arc/$this\" not found")

val ArcResource.text: String
    get() = Arc::class.java.getResourceAsStream("/assets/arc/$this")?.readAllBytes()?.decodeToString()
        ?: throw FileNotFoundException("File \"/assets/arc/$this\" not found")

val ArcResource.url: URL
    get() = Arc::class.java.getResource("/assets/arc/$this")
        ?: throw FileNotFoundException("File \"/assets/arc/$this\" not found")

fun ArcResource.readImage(): BufferedImage = ImageIO.read(this.stream)
