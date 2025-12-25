
package com.arc.util.extension

import net.minecraft.client.MinecraftClient

val MinecraftClient.partialTicks
    get() = tickDelta.toDouble()

val MinecraftClient.tickDelta
    get() = renderTickCounter.getTickProgress(true)
