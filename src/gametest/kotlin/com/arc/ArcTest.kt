
package com.arc

import com.arc.context.SafeContext
import com.arc.threading.runSafe
import com.arc.util.combat.DamageUtils.isFallDeadly
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext
import net.minecraft.client.gui.screen.world.WorldCreator

@Suppress("UnstableApiUsage")
object ArcTest : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) {
        val singleplayerContext = context.worldBuilder()
            .adjustSettings {
                it.gameMode = WorldCreator.Mode.CREATIVE
            }
            .create()

        val world = singleplayerContext.clientWorld
        val server = singleplayerContext.server

        world.waitForChunksDownload()

        server.runCommand("/tp Steve ~ ~30 ~")
        context.unit("assert deadly fall") { isFallDeadly() }

        server.runCommand("/tp Steve ~ -50 ~")
        context.unit("assert safe fall") { !isFallDeadly() }
        server.runCommand("/tp Steve ~ -60 ~")

        // All the tests passed
        singleplayerContext.close()
    }

    inline fun ClientGameTestContext.unit(label: String, crossinline block: SafeContext.() -> Boolean) {
        waitTick()

        runOnClient<IllegalStateException> {
            val asserted = runSafe(block)
                ?: throw IllegalStateException("Could not run in a safe context")

            check(asserted) { "Assertion failed: $label" }
        }
    }
}
