
package com.arc.module.modules.movement

import com.arc.context.SafeContext
import com.arc.event.events.ClientEvent
import com.arc.event.events.MovementEvent
import com.arc.event.events.TickEvent
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.managers.Request.Companion.submit
import com.arc.interaction.managers.rotating.Rotation
import com.arc.interaction.managers.rotating.RotationConfig
import com.arc.interaction.managers.rotating.RotationMode
import com.arc.interaction.managers.rotating.RotationRequest
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.util.NamedEnum
import com.arc.util.extension.contains
import com.arc.util.extension.isElytraFlying
import com.arc.util.player.MovementUtils.addSpeed
import com.arc.util.player.MovementUtils.calcMoveYaw
import com.arc.util.player.MovementUtils.handledByBaritone
import com.arc.util.player.MovementUtils.isInputting
import com.arc.util.player.MovementUtils.motionY
import com.arc.util.player.MovementUtils.moveDelta
import com.arc.util.player.MovementUtils.newMovementInput
import com.arc.util.player.MovementUtils.roundedForward
import com.arc.util.player.MovementUtils.roundedStrafing
import com.arc.util.player.MovementUtils.setSpeed
import com.arc.util.world.entitySearch
import net.minecraft.entity.vehicle.BoatEntity

object Speed : Module(
    name = "Speed",
    description = "Accelerates your walking speed",
    tag = ModuleTag.MOVEMENT,
) {
    @JvmStatic
    val mode by setting("Mode", Mode.GrimStrafe).onValueChange { _, _ -> reset() }

    // Grim
    private val diagonal by setting("Diagonal", true).group(Mode.GrimStrafe)
    private val grimBoatBoost by setting("Boat Boost", 0.4, 0.0..1.7, 0.01).group(Mode.GrimStrafe)

    // NCP
    private val strict by setting("Strict", true).group(Mode.NcpStrafe)
    private val lowerJump by setting("Lower Jump", true).group(Mode.NcpStrafe)
    private val ncpAutoJump by setting("Auto Jump", false).group(Mode.NcpStrafe)
    private val ncpTimerBoost by setting("Timer Boost", 1.08, 1.0..1.1, 0.01).group(Mode.NcpStrafe)

    override val rotationConfig = RotationConfig.Instant(RotationMode.Sync)

    // NCP state variables
    const val NCP_BASE_SPEED = 0.2873
    private const val NCP_AIR_DECAY = 0.9937
    private var ncpPhase = NCPPhase.SlowDown
    private var ncpSpeed = NCP_BASE_SPEED
    private var lastDistance = 0.0

    enum class Mode(override val displayName: String) : NamedEnum {
        GrimStrafe("Grim Strafe"),
        NcpStrafe("NCP Strafe"),
    }

    private enum class NCPPhase {
        Jump,
        JumpPost,
        SlowDown
    }

    init {
        listen<MovementEvent.Player.Pre> {
            if (!shouldWork()) {
                reset()
                return@listen
            }

            when (mode) {
                Mode.NcpStrafe -> handleStrafe()
                Mode.GrimStrafe -> handleGrim()
            }
        }

        listen<MovementEvent.Player.Post> {
            lastDistance = player.moveDelta
        }

        listen<ClientEvent.TimerUpdate> {
            if (mode != Mode.NcpStrafe) return@listen
            if (!shouldWork() || !isInputting) return@listen
            it.speed = ncpTimerBoost
        }

        listen<MovementEvent.Jump> {
            if (mode == Mode.NcpStrafe && shouldWork()) it.cancel()
        }

        listen<TickEvent.Pre> {
            if (mode != Mode.GrimStrafe || !shouldWork()) return@listen

            val input = newMovementInput()
            if (!input.isInputting) return@listen

            val intendedMoveYaw = calcMoveYaw(player.yaw, input.roundedForward, input.roundedStrafing)
            val targetYaw = if (diagonal && !(input.playerInput.jump && player.isOnGround)) {
                intendedMoveYaw - 45.0f
            } else intendedMoveYaw

            submit(RotationRequest(Rotation(targetYaw, player.pitch.toDouble()), this@Speed))
        }

        onEnable {
            reset()
        }
    }

    private fun SafeContext.handleGrim() {
        var grimSpeed = 0.0

        if (grimBoatBoost > 0.0) {
            grimSpeed += entitySearch<BoatEntity>(4.0) {
                player.boundingBox in it.boundingBox.expand(0.01)
            }.sumOf { grimBoatBoost }
        }

        addSpeed(
            if (isInputting) grimSpeed else 0.0
        )
    }

    private fun SafeContext.handleStrafe() {
        val shouldJump = player.input.playerInput.jump || (ncpAutoJump && isInputting)

        if (player.isOnGround && shouldJump) {
            ncpPhase = NCPPhase.Jump
        }

        ncpPhase = when (ncpPhase) {
            NCPPhase.Jump -> {
                if (player.isOnGround) {
                    player.motionY = if (lowerJump) 0.4 else 0.42
                    ncpSpeed = NCP_BASE_SPEED + 0.3
                    NCPPhase.JumpPost
                } else NCPPhase.SlowDown
            }

            NCPPhase.JumpPost -> {
                ncpSpeed *= if (strict) 0.59 else 0.62
                NCPPhase.SlowDown
            }

            NCPPhase.SlowDown -> {
                ncpSpeed = lastDistance * NCP_AIR_DECAY
                NCPPhase.SlowDown
            }
        }

        if (player.isOnGround && !shouldJump) {
            ncpSpeed = NCP_BASE_SPEED
        }

        ncpSpeed = ncpSpeed.coerceIn(NCP_BASE_SPEED..1.0)

        val moveSpeed = if (isInputting) ncpSpeed else {
            ncpSpeed = NCP_BASE_SPEED
            0.0
        }

        setSpeed(moveSpeed)
    }

    private fun SafeContext.shouldWork(): Boolean {
        if (player.abilities.flying
            || player.isElytraFlying
            || player.isTouchingWater
            || player.isInLava
            || player.isRiding) return false

        return when (mode) {
            Mode.GrimStrafe -> !player.input.handledByBaritone
            Mode.NcpStrafe -> !player.isSneaking
        }
    }

    private fun reset() {
        ncpPhase = NCPPhase.SlowDown
        ncpSpeed = NCP_BASE_SPEED
    }
}
