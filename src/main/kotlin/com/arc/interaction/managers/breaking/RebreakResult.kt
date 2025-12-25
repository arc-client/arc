
package com.arc.interaction.managers.breaking

sealed class RebreakResult {
    data object Ignored : RebreakResult()

    data object Rebroke : RebreakResult()

    class StillBreaking(
        val breakInfo: BreakInfo
    ) : RebreakResult()
}