package com.diegoalvis.dualcalculator

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

internal class MainViewModel(private val cache: Cache) : ViewModel() {

    // Pos 0 -> screen 0 , Pos 1 -> screen 1, ...
    private val _uiState = MutableLiveData<List<UiState>>()
    val uiState: LiveData<List<UiState>> = _uiState

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
            val storedInfo = cache.readExpressions()?.map {
                computeOperation(it)
            } ?: listOf(UiState())
            _uiState.value = storedInfo
        }
    }

    fun updateConfig(isLandscape: Boolean) {
        if (isLandscape && _uiState.value?.size == 1) {
            _uiState.value = _uiState.value?.toMutableList()?.plus(UiState())
        }
    }

    fun reduce(event: UiEvents) {
        viewModelScope.launch {
            when (event) {
                is UiEvents.Calculate -> {
                    val uiState = computeOperation(event.input)
                    _uiState.value?.toMutableList()?.apply {
                        set(event.screenPosition, uiState)
                    }?.let { newValue ->
                        _uiState.value = newValue
                        // Save result
                        val expressions = newValue.map { it.input }
                        cache.writeExpressions(expressions)
                    }
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

    private suspend fun computeOperation(input: String): UiState {
        return withContext(Dispatchers.Default) { // Could use IO as the calculator only has basic operations
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

            return@withContext UiState(
                input = input,
                result = result,
                outputTextColor = outputTextColor,
            )
        }
    }
}

private fun Double.getFormattedOutput(): String {
    return DecimalFormat("0.######").format(this).toString()
}