
package com.arc.context

import com.arc.config.groups.BuildConfig
import com.arc.config.groups.EatConfig
import com.arc.interaction.managers.breaking.BreakConfig
import com.arc.interaction.managers.hotbar.HotbarConfig
import com.arc.interaction.managers.interacting.InteractConfig
import com.arc.interaction.managers.inventory.InventoryConfig
import com.arc.interaction.managers.rotating.RotationConfig

interface Automated {
    val buildConfig: BuildConfig
    val breakConfig: BreakConfig
    val interactConfig: InteractConfig
    val rotationConfig: RotationConfig
    val inventoryConfig: InventoryConfig
    val hotbarConfig: HotbarConfig
    val eatConfig: EatConfig
}