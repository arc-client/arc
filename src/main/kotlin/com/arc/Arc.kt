
package com.arc

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.arc.config.serializer.BlockPosCodec
import com.arc.config.serializer.BlockCodec
import com.arc.config.serializer.ColorSerializer
import com.arc.config.serializer.GameProfileCodec
import com.arc.config.serializer.ItemCodec
import com.arc.config.serializer.ItemStackCodec
import com.arc.config.serializer.KeyCodeCodec
import com.arc.config.serializer.OptionalCodec
import com.arc.core.Loader
import com.arc.event.events.ClientEvent
import com.arc.event.listener.UnsafeListener.Companion.listenOnceUnsafe
import com.arc.gui.components.ClickGuiLayout
import com.arc.util.KeyCode
import com.arc.util.WindowUtils.setArcWindowIcon
import com.mojang.authlib.GameProfile
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ArrowItem
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.PotionItem
import net.minecraft.item.RangedWeaponItem
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Color
import java.util.*


object Arc : ClientModInitializer {
    const val MOD_NAME = "Arc"
    const val MOD_ID = "arc"
    const val SYMBOL = "λ"
    const val APP_ID = "1221289599427416127"
    const val REPO_URL = "https://github.com/arc-client/arc"
    val VERSION: String = FabricLoader.getInstance()
        .getModContainer("arc").orElseThrow()
        .metadata.version.friendlyString

    val LOG: Logger = LogManager.getLogger(SYMBOL)

    @JvmStatic
    val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

    val isDebug = System.getProperty("arc.dev") != null

    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(KeyCode::class.java, KeyCodeCodec)
        .registerTypeAdapter(Color::class.java, ColorSerializer)
        .registerTypeAdapter(BlockPos::class.java, BlockPosCodec)
        .registerTypeAdapter(Block::class.java, BlockCodec)
        .registerTypeAdapter(GameProfile::class.java, GameProfileCodec)
        .registerTypeAdapter(Optional::class.java, OptionalCodec)
        .registerTypeAdapter(ItemStack::class.java, ItemStackCodec)
        .registerTypeAdapter(Text::class.java, Text.Serializer(DynamicRegistryManager.EMPTY))
        .registerTypeAdapter(Item::class.java, ItemCodec)
        .registerTypeAdapter(BlockItem::class.java, ItemCodec)
        .registerTypeAdapter(ArrowItem::class.java, ItemCodec)
        .registerTypeAdapter(PotionItem::class.java, ItemCodec)
        .registerTypeAdapter(RangedWeaponItem::class.java, ItemCodec)
        .create()

    override fun onInitializeClient() {} // nop

    init {
        // We want the opengl context to be created
        listenOnceUnsafe<ClientEvent.Startup>(priority = Int.MAX_VALUE) {
            LOG.info("$MOD_NAME $VERSION initialized in ${Loader.initialize()} ms\n")
            if (ClickGuiLayout.setArcWindowIcon) setArcWindowIcon()
            true
        }
    }
}
