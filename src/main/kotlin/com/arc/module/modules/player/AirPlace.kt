
package com.arc.module.modules.player

import com.arc.config.AutomationConfig.Companion.setDefaultAutomationConfig
import com.arc.config.applyEdits
import com.arc.config.settings.complex.Bind
import com.arc.context.SafeContext
import com.arc.event.events.MouseEvent
import com.arc.event.events.PlayerEvent
import com.arc.event.events.TickEvent
import com.arc.event.events.onStaticRender
import com.arc.event.listener.SafeListener.Companion.listen
import com.arc.interaction.construction.simulation.BuildSimulator.simulate
import com.arc.interaction.construction.simulation.context.BuildContext
import com.arc.interaction.construction.verify.TargetState
import com.arc.interaction.managers.interacting.InteractConfig
import com.arc.interaction.managers.interacting.InteractRequest.Companion.interactRequest
import com.arc.interaction.managers.rotating.Rotation.Companion.rotation
import com.arc.module.Module
import com.arc.module.tag.ModuleTag
import com.arc.threading.runSafeAutomated
import com.arc.util.InputUtils.isSatisfied
import com.arc.util.KeyCode
import com.arc.util.NamedEnum
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.RaycastContext
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.util.concurrent.ConcurrentLinkedQueue

object AirPlace : Module(
	name = "AirPlace",
	description = "Allows placing blocks in air",
	tag = ModuleTag.PLAYER
) {
	enum class Group(override val displayName: String) : NamedEnum {
		General("General"),
		Render("Render")
	}

	private var distance by setting("Distance", 4.0, 1.0..7.0, 1.0).group(Group.General)
	private val scrollBind by setting("Scroll Bind", Bind(KeyCode.Unbound.code, GLFW.GLFW_MOD_CONTROL), "Allows you to hold the ctrl key and scroll to adjust distance").group(Group.General)

	private val outlineColor by setting("Outline Color", Color.WHITE).group(Group.Render)

	private var placementPos: BlockPos? = null
	private var placementState: BlockState? = null
	private val pendingInteractions = ConcurrentLinkedQueue<BuildContext>()

	init {
		setDefaultAutomationConfig {
			applyEdits {
				interactConfig.apply {
					::airPlace.edit { defaultValue(InteractConfig.AirPlaceMode.Grim) }
				}
				hideAllGroupsExcept(interactConfig)
			}
		}

		listen<TickEvent.Pre> {
			val selectedStack = player.inventory.selectedStack
			val blockItem = selectedStack.item as? BlockItem ?: run {
				placementPos = null
				placementState = null
				return@listen
			}
			val raycastEnd = player.rotation.vector.multiply(distance).add(player.eyePos)
			val raycastContext = RaycastContext(
				player.eyePos,
				raycastEnd,
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE,
				player
			)
			val placementContext = ItemPlacementContext(
				world,
				player, Hand.MAIN_HAND,
				selectedStack,
				world.raycast(raycastContext),
			)
			placementPos = placementContext.blockPos
			placementState = blockItem.getPlacementState(placementContext)
		}

		listen<PlayerEvent.Interact.Block> { if (airPlace()) it.cancel() }
		listen<PlayerEvent.Interact.Item> { if (airPlace()) it.cancel() }

		onStaticRender { event ->
			placementPos?.let { pos ->
				val boxes = placementState?.getOutlineShape(world, pos)?.boundingBoxes
					?: listOf(Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
				boxes.forEach { box ->
					event.outline(box.offset(pos), outlineColor)
				}
			}
		}

		listen<MouseEvent.Scroll> { event ->
			if (!scrollBind.isSatisfied()) return@listen
			event.cancel()
			distance += event.delta.y
		}
	}

	private fun SafeContext.airPlace(): Boolean {
		if (player.inventory.selectedStack.item !is BlockItem) return false
		placementPos?.let { pos ->
			placementState?.let { state ->
				runSafeAutomated {
					mapOf(pos to TargetState.State(state))
						.simulate()
						.interactRequest(pendingInteractions)
						?.submit()
				}
				return true
			}
		}
		return false
	}
}