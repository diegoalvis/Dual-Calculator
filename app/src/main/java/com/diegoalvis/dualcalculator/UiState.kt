package com.diegoalvis.dualcalculator

import androidx.annotation.ColorRes

enum class CommandDirection { LEFT, RIGHT }

internal data class UiState(
    val input: String = "",
    val result: String = "",
    @ColorRes val outputTextColor: Int = R.color.green,
)

internal sealed class UiEvents {
    data class Calculate(
        val screenPosition: Int,
        val input: String,
    ) : UiEvents()

    data class SendToNextScreen(
        val direction: CommandDirection,
    ) : UiEvents()
}