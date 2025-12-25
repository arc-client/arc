
package com.arc.interaction.managers

import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos

interface LogContext {
    fun getLogContextBuilder(): LogContextBuilder.() -> Unit

    companion object {
        @DslMarker
        private annotation class LogContextDsl

        fun BlockPos.getLogContextBuilder(): LogContextBuilder.() -> Unit {
            val pos = if (this is BlockPos.Mutable) toImmutable() else this
            return { value("Block Pos", pos.toShortString()) }
        }

        fun BlockHitResult.getLogContextBuilder(): LogContextBuilder.() -> Unit = {
            group("Block Hit Result") {
                value("Side", side)
                text(blockPos.getLogContextBuilder())
                value("Pos", pos)
            }
        }

        @LogContextDsl
        fun buildLogContext(tabMin: Int = 0, builder: LogContextBuilder.() -> Unit): String =
            LogContextBuilder(tabMin).apply(builder).build()

        @LogContextDsl
        private fun LogContextBuilder.build() = logContext

        @LogContextDsl
        class LogContextBuilder(val tabMin: Int = 0) {
            var logContext = ""

            private var tabs = tabMin

            @LogContextDsl
            fun sameLine() {
                val length = logContext.length
                val first = logContext[length - 2]
                val second = logContext[length - 1]
                if (first != '\\' || second != 'n') throw IllegalStateException("String does not end in a new line")
                logContext = logContext.removeRange(length - 2, length - 1)
            }

            @LogContextDsl
            fun text(text: String) {
                repeat(tabs) {
                    logContext += "----"
                }
                logContext += "$text\n"
            }

            @LogContextDsl
            fun text(builder: LogContextBuilder.() -> Unit) {
                logContext += LogContextBuilder(tabs).apply(builder).build()
            }

            @LogContextDsl
            fun value(name: String, value: Any) {
                text("$name: $value")
            }

            @LogContextDsl
            fun value(name: String, value: String) {
                text("$name: $value")
            }

            @LogContextDsl
            fun group(name: String, builder: LogContextBuilder.() -> Unit) {
                text("$name {")
                logContext += LogContextBuilder(tabs + 1).apply(builder).build()
                text("}")
            }

            @LogContextDsl
            fun pushTab() {
                tabs++
            }

            @LogContextDsl
            fun popTab() {
                tabs--
                if (tabs < tabMin) throw IllegalStateException("Cannot reduce tabs beneath the minimum tab count")
            }
        }
    }
}