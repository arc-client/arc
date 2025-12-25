
package com.arc.module.modules.render

import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafe
import com.arc.util.NamedEnum
import net.minecraft.world.World

object Weather : Module(
    name = "Weather",
    description = "Modifies the client side weather",
    tag = ModuleTag.RENDER
) {
    private enum class Group(override val displayName: String) : NamedEnum {
        Overworld("Overworld"),
        Nether("Nether"),
        End("End")
    }

    @JvmStatic val overworldMode by setting("Overworld Mode", WeatherMode.Clear).group(Group.Overworld)
    @JvmStatic val overrideSnow by setting("Override Snow", false) { overworldMode == WeatherMode.Rain }.group(Group.Overworld)
    @JvmStatic val netherMode by setting("Nether Mode", WeatherMode.Clear).group(Group.Nether)
    @JvmStatic val endMode by setting("End Mode", WeatherMode.Clear).group(Group.End)

    @JvmStatic fun getWeatherMode() =
        runSafe {
            val dimension = world.registryKey
            when (dimension) {
                World.OVERWORLD -> overworldMode
                World.NETHER -> netherMode
                else -> endMode
            }
        } ?: WeatherMode.Clear

    enum class WeatherMode {
        Clear,
        Rain,
        Thunder,
        Snow
    }
}