package com.diegoalvis.dualcalculator

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

internal class MainViewModel(private val cache: Cache) : ViewModel() {

    // Pos 0 -> screen 0 , Pos 1 -> screen 1, ...
    private val _uiState = MutableStateFlow(listOf(UiState()))
    val uiState: StateFlow<List<UiState>> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = (this[APPLICATION_KEY] as Application).applicationContext
                val cache: Cache = CacheImpl(context = context, preferencesName = "session_pref")
                MainViewModel(cache = cache)
            }
        }
    }

    init {
        viewModelScope.launch {
            val storedInfo = cache.readExpressions()
                ?.map { computeOperation(it) }
                ?.takeUnless { it.isEmpty() }
                ?: listOf(UiState())

            _uiState.emit(storedInfo)
        }
    }

    fun updateConfig(isLandscape: Boolean) {
        viewModelScope.launch {
            if (isLandscape && _uiState.value.size == 1) {
                _uiState.emit(_uiState.value.toMutableList().plus(UiState()))
            }
        }
    }

    fun onEvent(event: UiEvents) { // Reducer
        viewModelScope.launch {
            when (event) {
                is UiEvents.Calculate -> {
                    val uiState = computeOperation(event.input)
                    val newValue = _uiState.value.toMutableList().apply {
                        set(event.screenPosition, uiState)
                    }
                    _uiState.emit(newValue)
                    saveCurrentValues(newValue)
                }

                is UiEvents.SendToNextScreen -> {
                    transferInfoBetweenCalculators(event)
                }
            }
        }
    }

    private fun transferInfoBetweenCalculators(event: UiEvents.SendToNextScreen) {
        viewModelScope.launch {
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
            val copyUiState = _uiState.value[sourceIndex]
            val newValue = _uiState.value.toMutableList().apply {
                set(targetIndex, copyUiState)
            }
            _uiState.emit(newValue.toList())
            saveCurrentValues(newValue)
        }
    }

    private fun computeOperation(input: String): UiState {
        var result: String
        var outputTextColor: Int
        try {
            val numericResult = Expression(input).calculate()
            when {
                input.isEmpty() -> {
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

        return UiState(
            input = input,
            result = result,
            outputTextColor = outputTextColor,
        )
    }

    private suspend fun saveCurrentValues(newValue: MutableList<UiState>) {
        val expressions = newValue.map { it.input }
        cache.writeExpressions(expressions)
    }
}

fun Double.getFormattedOutput(): String {
    return DecimalFormat("0.######").format(this).toString()
}