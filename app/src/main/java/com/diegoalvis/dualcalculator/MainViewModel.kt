package com.diegoalvis.dualcalculator

import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

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

internal class MainViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // Pos 0 -> screen 0 , Pos 1 -> screen 1, ...
    private val _uiState: MutableLiveData<List<UiState>> = savedStateHandle.getLiveData("expression", listOf(UiState()))
    val uiState: LiveData<List<UiState>> = _uiState

    // TODO implement dynamically way to load initial values
    fun initialSetup(isLandscape: Boolean) {
        if (isLandscape && _uiState.value?.size == 1) {
            _uiState.value = _uiState.value?.toMutableList()?.plus(UiState())
        }
    }

    fun reduce(event: UiEvents) {
        viewModelScope.launch {
            when (event) {
                is UiEvents.Calculate -> {
                    computeOperation(event)
                }
                is UiEvents.SendToNextScreen -> {
                    transferInfoBetweenCalculators(event)
                }
            }
        }
    }

    private fun transferInfoBetweenCalculators(event: UiEvents.SendToNextScreen) {
        val sourceIndex: Int
        val targetIndex: Int
        when (event.direction) {
            CommandDirection.LEFT -> {
                sourceIndex = 1
                targetIndex = 0
            }
            CommandDirection.RIGHT -> {
                sourceIndex = 0
                targetIndex = 1
            }
        }
        val copyUiState = _uiState.value?.get(sourceIndex) ?: return
        val newValue = _uiState.value?.toMutableList()?.apply {
            set(targetIndex, copyUiState)
        }
        _uiState.value = newValue?.toList()
    }

    private suspend fun computeOperation(event: UiEvents.Calculate) {
        withContext(Dispatchers.Default) { // Could use IO as the calculator only has basic operations
            var result: String
            var outputTextColor: Int
            try {
                val numericResult = Expression(event.input).calculate()
                when {
                    event.input.isEmpty() -> {
                        result = ""
                        outputTextColor = R.color.green
                    }

                    numericResult.isNaN() -> {
                        outputTextColor = R.color.red
                        result = "Bad exp"
                    }

                    else -> {
                        outputTextColor = R.color.green
                        result = numericResult.getFormattedOutput()
                    }
                }
            } catch (e: Exception) {
                outputTextColor = R.color.red
                result = "Error"
            }
            val uiState = UiState(
                input = event.input,
                result = result,
                outputTextColor = outputTextColor,
            )
            withContext(Dispatchers.Main) {
                val newValue = _uiState.value?.toMutableList()?.apply {
                    set(event.screenPosition, uiState)
                }
                _uiState.value = newValue?.toList()
            }
        }
    }
}


private fun Double.getFormattedOutput(): String {
    return DecimalFormat("0.######").format(this).toString()
}


