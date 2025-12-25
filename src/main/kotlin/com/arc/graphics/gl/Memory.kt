
package com.arc.graphics.gl

import java.nio.ByteBuffer

val Int.kilobyte get() = this * 1000
val Int.megabyte get() = this * 1000 * 1000
val Int.gigabyte get() = this * 1000 * 1000 * 1000

val Int.kibibyte get() = this * 1024
val Int.mebibyte get() = this * 1024 * 1024
val Int.gibibyte get() = this * 1024 * 1024 * 1024

val Long.kilobyte get() = this * 1000
val Long.megabyte get() = this * 1000 * 1000
val Long.gigabyte get() = this * 1000 * 1000 * 1000

val Long.kibibyte get() = this * 1024
val Long.mebibyte get() = this * 1024 * 1024
val Long.gibibyte get() = this * 1024 * 1024 * 1024

fun ByteBuffer.putTo(dst: ByteBuffer?) { dst?.put(this) }
