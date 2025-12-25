
package com.arc.interaction.managers.rotating

import com.arc.Arc.mc
import com.arc.context.AutomatedSafeContext
import com.arc.context.SafeContext
import com.arc.event.events.ConnectionEvent
import com.arc.event.events.PacketEvent
import com.arc.event.events.PlayerEvent
import com.arc.event.events.RenderEvent
import com.arc.event.events.TickEvent
import com.arc.event.events.TickEvent.Companion.ALL_STAGES
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.event.listener.UnsafeListener.Companion.listenUnsafe
import com.arc.interaction.BaritoneManager
import com.arc.interaction.managers.Logger
import com.arc.interaction.managers.Manager
import com.arc.interaction.managers.rotating.Rotation.Companion.slerp
import com.arc.interaction.managers.rotating.RotationManager.activeRequest
import com.arc.interaction.managers.rotating.RotationManager.activeRotation
import com.arc.interaction.managers.rotating.RotationManager.serverRotation
import com.arc.interaction.managers.rotating.RotationManager.updateActiveRotation
import com.arc.module.hud.ManagerDebugLoggers.rotationManagerLogger
import com.arc.threading.runGameScheduled
import com.arc.threading.runSafe
import com.arc.util.extension.rotation
import com.arc.util.math.MathUtils.toRadian
import com.arc.util.math.Vec2d
import com.arc.util.math.lerp
import com.arc.util.world.raycast.RayCastUtils.orMiss
import net.minecraft.client.input.Input
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.PlayerInput
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec2f
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manager designed to rotate the player and adjust movement input to match the camera's direction.
 */
object RotationManager : Manager<RotationRequest>(
    1,
	*(ALL_STAGES.subList(ALL_STAGES.indexOf(TickEvent.Player.Post), ALL_STAGES.size - 1).toTypedArray()),
), Logger {
    var activeRotation = Rotation.ZERO
    var serverRotation = Rotation.ZERO
    @JvmStatic var prevServerRotation = Rotation.ZERO
    private var usingBaritoneRotation = false

    var activeRequest: RotationRequest? = null
    private var changedThisTick = false

    override val logger = rotationManagerLogger

    override fun load(): String {
        super.load()

        listen<TickEvent.Pre>(priority = Int.MAX_VALUE) {
            activeRequest?.let {
                if (it.keepTicks <= 0 && it.decayTicks <= 0) {
                    logger.debug("Clearing active request", it)
                    activeRequest = null
                }
            }
        }

        listen<TickEvent.Post>(priority = Int.MIN_VALUE) {
            usingBaritoneRotation = false
            activeRequest?.let { request ->
                request.age++
            }
            changedThisTick = false
        }

        listen<RenderEvent.UpdateTarget> {
            if (activeRequest == null) return@listen
            it.cancel()

            val eye = player.eyePos
            val entityHit = player.rotation.rayCast(player.entityInteractionRange, eye).orMiss
            mc.targetedEntity = (entityHit as? EntityHitResult)?.entity
            val blockHit = player.rotation.rayCast(player.blockInteractionRange, eye).orMiss
            mc.crosshairTarget = blockHit
        }

        listen<PacketEvent.Receive.Post>(priority = Int.MIN_VALUE) { event ->
            val packet = event.packet
            if (packet !is PlayerPositionLookS2CPacket) return@listen

            runGameScheduled {
                reset(Rotation(packet.change.yaw, packet.change.pitch))
            }
        }

        listenUnsafe<ConnectionEvent.Connect.Pre>(priority = Int.MIN_VALUE) {
            reset(Rotation.ZERO)
        }

        // Override user interactions with max priority
        listen<PlayerEvent.Interact.Block>(priority = Int.MAX_VALUE) {
            activeRotation = player.rotation
        }

        listen<PlayerEvent.Attack.Block>(priority = Int.MAX_VALUE) {
            activeRotation = player.rotation
        }

        listen<PlayerEvent.Interact.Entity>(priority = Int.MAX_VALUE) {
            activeRotation = player.rotation
        }

        listen<PlayerEvent.Attack.Entity>(priority = Int.MAX_VALUE) {
            activeRotation = player.rotation
        }

        listen<PlayerEvent.Interact.Item>(priority = Int.MAX_VALUE) {
            activeRotation = player.rotation
        }

        return "Loaded Rotation Manager"
    }

    /**
     * If the [activeRequest] is from an older tick or null, and the [request]'s target rotation is not null,
     * the request is accepted and set as the [activeRequest]. The [activeRotation] is then updated.
     *
     * @see updateActiveRotation
     */
    override fun AutomatedSafeContext.handleRequest(request: RotationRequest) {
        activeRequest?.let { if (it.age <= 0) return }
        if (request.rotation.value != null) {
            logger.debug("Accepting request", request)
            activeRequest = request
            updateActiveRotation()
            changedThisTick = true
        }
    }

    /**
     * If the rotation has not been changed this tick, the [activeRequest]'s target rotation is updated, and
     * likewise the [activeRotation]. The [activeRequest] is then updated, ticking the [RotationRequest.keepTicks]
     * and [RotationRequest.decayTicks].
     */
    @JvmStatic
    fun processRotations() = runSafe {
        if (activeRequest != null) activeThisTick = true

        if (!changedThisTick) { // rebuild the rotation if the same context gets used again
            activeRequest?.rotation?.update()
            updateActiveRotation()
        }
    }

    @JvmStatic
    fun handleBaritoneRotation(yaw: Double, pitch: Double) {
        runSafe {
            usingBaritoneRotation = true
            activeRequest = RotationRequest(Rotation(yaw, pitch), BaritoneManager)
            updateActiveRotation()
            changedThisTick = true
        }
    }

    /**
     * Calculates and sets the optimal movement input for moving in the direction the player is facing. This
     * is not the direction the rotation manager is rotated towards, but the underlying player rotation, typically
     * also the camera's rotation.
     */
    @JvmStatic
    fun redirectStrafeInputs(input: Input) = runSafe {
        if (usingBaritoneRotation) return@runSafe

        val movementYaw = movementYaw ?: return@runSafe
        val playerYaw = player.yaw

        if (movementYaw.minus(playerYaw).rem(360f).let { it * it } < 0.001f) return@runSafe

        val originalStrafe = input.movementVector.x
        val originalForward = input.movementVector.y

        if (originalStrafe == 0.0f && originalForward == 0.0f) return@runSafe

        val deltaYawRad = (playerYaw - movementYaw).toRadian()

        val cos = cos(deltaYawRad)
        val sin = sin(deltaYawRad)
        val newStrafe = originalStrafe * cos - originalForward * sin
        val newForward = originalStrafe * sin + originalForward * cos

        val angle = atan2(newStrafe.toDouble(), newForward.toDouble())

        // Define the boundaries for our 8 sectors (in radians). Each sector is 45 degrees (PI/4).
        val sector = (PI / 4.0).toFloat()
        val boundary = (PI / 8.0).toFloat() // The halfway point between sectors (22.5 degrees)

        var pressForward = false
        var pressBackward = false
        var pressLeft = false
        var pressRight = false

        // Determine which 45-degree sector the angle falls into and set the corresponding keys.
        when {
            angle > -boundary && angle <= boundary -> {
                pressForward = true
            }
            angle > boundary && angle <= boundary + sector -> {
                pressForward = true
                pressLeft = true
            }
            angle > boundary + sector && angle <= boundary + 2 * sector -> {
                pressLeft = true
            }
            angle > boundary + 2 * sector && angle <= boundary + 3 * sector -> {
                pressBackward = true
                pressLeft = true
            }
            angle > boundary + 3 * sector || angle <= -(boundary + 3 * sector) -> {
                pressBackward = true
            }
            angle > -(boundary + 3 * sector) && angle <= -(boundary + 2 * sector) -> {
                pressBackward = true
                pressRight = true
            }
            angle > -(boundary + 2 * sector) && angle <= -(boundary + sector) -> {
                pressRight = true
            }
            angle > -(boundary + sector) && angle <= -boundary -> {
                pressForward = true
                pressRight = true
            }
        }

        input.playerInput = PlayerInput(
            pressForward,
            pressBackward,
            pressLeft,
            pressRight,
            input.playerInput.jump(),
            input.playerInput.sneak(),
            input.playerInput.sprint()
        )

        val x = multiplier(input.playerInput.left(), input.playerInput.right())
        val y = multiplier(input.playerInput.forward(), input.playerInput.backward())
        input.movementVector = Vec2f(x, y).normalize()
    }

    private fun multiplier(positive: Boolean, negative: Boolean) =
        ((if (positive) 1 else 0) - (if (negative) 1 else 0)).toFloat()

    fun onRotationSend() {
        prevServerRotation = serverRotation
        serverRotation = activeRotation

        if (activeRequest?.rotationConfig?.rotationMode == RotationMode.Lock) {
            mc.player?.yaw = serverRotation.yawF
            mc.player?.pitch = serverRotation.pitchF
        }
    }

    /**
     * Updates the [activeRotation]. If [activeRequest] is null, the player's rotation is used.
     * Otherwise, the [serverRotation] is interpolated towards the [RotationRequest.target] rotation.
     */
    private fun SafeContext.updateActiveRotation() {
        activeRotation = activeRequest?.let { active ->
            val rotationTo = if (active.keepTicks >= 0)
                active.rotation.value ?: activeRotation // the same context gets used again && the rotation is null this tick
            else player.rotation

            if (active.keepTicks-- <= 0) {
                active.decayTicks--
            }

            // Important: do NOT wrap the result yaw; keep it continuous to match vanilla packets
            serverRotation.slerp(rotationTo, active.rotationConfig.turnSpeed)
        } ?: player.rotation

        logger.debug("Active rotation set to $activeRotation", activeRequest)
    }

    private fun reset(rotation: Rotation) {
        logger.debug("Resetting values with rotation $rotation")
        prevServerRotation = rotation
        serverRotation = rotation
        activeRotation = rotation
        activeRequest = null
    }

    @JvmStatic
    val lockRotation
        get() = activeRequest?.let { active ->
            activeRotation.takeIf { active.rotationConfig.rotationMode == RotationMode.Lock && active.keepTicks > 0 && active.decayTicks > 0 }
        }

    @JvmStatic
    val headYaw
        get() = activeRotation.yawF.takeIf { activeRequest != null }

    @JvmStatic
    val headPitch
        get() = activeRotation.pitchF.takeIf { activeRequest != null }

    @JvmStatic
    val handYaw
        get() = activeRotation.yawF.takeIf { activeRequest?.rotationConfig?.rotationMode == RotationMode.Lock }

    @JvmStatic
    val handPitch
        get() = activeRotation.pitchF.takeIf { activeRequest?.rotationConfig?.rotationMode == RotationMode.Lock }

    @JvmStatic
    val movementYaw: Float?
        get() {
            return if (activeRequest == null || activeRequest?.rotationConfig?.rotationMode == RotationMode.Silent) null
            else activeRotation.yaw.toFloat()
        }

    @JvmStatic
    val movementPitch: Float?
        get() {
            return if (activeRequest == null || activeRequest?.rotationConfig?.rotationMode == RotationMode.Silent) null
            else activeRotation.pitch.toFloat()
        }

    @JvmStatic
    fun getRotationForVector(deltaTime: Double): Vec2d? {
        if (activeRequest == null || activeRequest?.rotationConfig?.rotationMode == RotationMode.Silent) return null

        val rot = lerp(deltaTime, serverRotation, activeRotation)
        return Vec2d(rot.yaw, rot.pitch)
    }
}
