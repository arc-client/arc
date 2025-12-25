
package com.arc.interaction.managers.breaking

import com.arc.config.ISettingGroup
import com.arc.config.groups.ActionConfig
import com.arc.config.groups.BuildConfig
import com.arc.util.Describable
import com.arc.util.NamedEnum
import net.minecraft.block.Block
import java.awt.Color

interface BreakConfig : ActionConfig, ISettingGroup {
    val breakMode: BreakMode
    val rebreak: Boolean

    val doubleBreak: Boolean
    val unsafeCancels: Boolean

    val breakThreshold: Float
    val fudgeFactor: Int
    val serverSwapTicks: Int
    //ToDo: Needs a more advanced player simulation implementation to predict the next ticks onGround / submerged status
//    abstract val desyncFix: Boolean
    val breakDelay: Int

    val swapMode: SwapMode

    val swing: SwingMode
    val swingType: BuildConfig.SwingType

    val rotate: Boolean

    val breakConfirmation: BreakConfirmationMode
    val breaksPerTick: Int

    val avoidLiquids: Boolean
    val avoidSupporting: Boolean
    val ignoredBlocks: Collection<Block>

    val efficientOnly: Boolean
    val suitableToolsOnly: Boolean
    val forceSilkTouch: Boolean
    val forceFortunePickaxe: Boolean
    val minFortuneLevel: Int

    val useWoodenTools: Boolean
    val useStoneTools: Boolean
    val useIronTools: Boolean
    val useDiamondTools: Boolean
    val useGoldTools: Boolean
    val useNetheriteTools: Boolean

    val sounds: Boolean
    val particles: Boolean
    val breakingTexture: Boolean

    val renders: Boolean
    val fill: Boolean
    val outline: Boolean
    val outlineWidth: Int
    val animation: AnimationMode

    val dynamicFillColor: Boolean
    val staticFillColor: Color
    val startFillColor: Color
    val endFillColor: Color

    val dynamicOutlineColor: Boolean
    val staticOutlineColor: Color
    val startOutlineColor: Color
    val endOutlineColor: Color

    enum class BreakMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        Vanilla("Vanilla", "Uses vanilla breaking"),
        Packet("Packet", "Breaks blocks using only using packets")
    }

    enum class SwapMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        None("None", "Never auto-swap tools. Keeps whatever you’re holding"),
        Start("Start", "Auto-swap to the best tool right when the break starts. No further swaps during the same break"),
        End("End", "Stay on your current tool at first, then auto-swap to the best tool right before the block finishes breaking to speed up the final stretch"),
        StartAndEnd("Start and End", "Auto-swap to the best tool at the start, and again right before the block finishes breaking if it would be faster"),
        Constant("Constant", "Always keep the best tool selected for the entire break. Swaps as needed to maintain optimal speed");

        fun isEnabled() = this != None
    }

    enum class SwingMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        Constant("Constant", "Swings the hand every tick"),
        StartAndEnd("Start and End", "Swings the hand at the start and end of breaking"),
        Start("Start", "Swings the hand at the start of breaking"),
        End("End", "Swings the hand at the end of breaking"),
        None("None", "Does not swing the hand at all");

        fun isEnabled() = this != None
    }

    enum class BreakConfirmationMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        None("No confirmation", "Breaks immediately without waiting for the server. Lowest latency, but can briefly show break effects even if the server later disagrees."),
        BreakThenAwait("Break now, confirm later", "Shows the break effects right away (particles/sounds) and then waits for the server to confirm. Feels instant while keeping results consistent."),
        AwaitThenBreak("Confirm first, then break", "Waits for the server response before showing break effects. Most accurate and safest, but adds a short delay.");
    }

    enum class AnimationMode(
        override val displayName: String,
        override val description: String
    ) : NamedEnum, Describable {
        None("None", "Does not render any breaking animation"),
        Out("Out", "Renders a growing animation"),
        In("In", "Renders a shrinking animation"),
        OutIn("Out In", "Renders a growing and shrinking animation"),
        InOut("In Out", "Renders a shrinking and growing animation")
    }
}
