package com.diegoalvis.dualcalculator

import androidx.lifecycle.ViewModel
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mariuszgromada.math.mxparser.Expression
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest : ViewModel() {


    private val testDispatcher = UnconfinedTestDispatcher()
    private val cache: Cache = mockk(relaxed = true)
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        viewModel = MainViewModel(cache = cache)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }


    @Test
    fun `onEvent Calculate calculates the result for a correct expression portrait`() = runTest(testDispatcher) {
        val pos = 0
        val expression = "21312+993"
        val expected = UiState(
            input = expression,
            result = calculateResult(expression),
            outputTextColor = R.color.green,
        )

        viewModel.updateConfig(isLandscape = false)
        viewModel.uiState.drop(1) // drop the first emit due a configuration change
        viewModel.onEvent(UiEvents.Calculate(pos, expression))

        assertEquals(expected, viewModel.uiState.first()[pos])

        coVerify {
            cache.readExpressions()
            cache.writeExpressions(any())
        }
    }

    @Test
    fun `onEvent Calculate calculates the result for a correct expression landscape`() = runTest(testDispatcher) {
        val pos = 1
        val expression = "93*1231/34.5"
        val expected = UiState(
            input = expression,
            result = calculateResult(expression),
            outputTextColor = R.color.green,
        )

        viewModel.updateConfig(isLandscape = true)
        viewModel.onEvent(UiEvents.Calculate(pos, expression))

        assertEquals(expected, viewModel.uiState.first()[pos])
        coVerify {
            cache.readExpressions()
            cache.writeExpressions(any())
        }
    }

    @Test
    fun `onEvent Calculate display error when wrong mathematical expression`() = runTest(testDispatcher) {
        val pos = 0
        val expression = ")123+34"
        val expected = UiState(
            input = expression,
            result = MainViewModel.BAD_EXPRESSION_MSG,
            outputTextColor = R.color.red,
        )

        viewModel.updateConfig(isLandscape = false)
        viewModel.uiState.drop(1) // drop the first emit due a configuration change
        viewModel.onEvent(UiEvents.Calculate(pos, expression))

        assertEquals(expected, viewModel.uiState.first()[pos])
    }

    @Test
    fun `viewModel load information previously saved in Cache`() = runTest(testDispatcher) {
        val expressions = listOf("123+34", "23434/324+2342*34.4")
        coEvery { cache.readExpressions() } returns expressions
        val expected = expressions.map {
            UiState(
                input = it,
                result = calculateResult(it),
                outputTextColor = R.color.green
            )
        }
        viewModel.updateConfig(isLandscape = true)

        assertEquals(expected, viewModel.uiState.first())
    }

    @Test
    fun `onEvent SendToNextScreen duplicates value in position 0 to position 1 when Direction Right`() = runTest(testDispatcher) {
        val expressions = listOf("123+34", "23434/324+2342*34.4")
        coEvery { cache.readExpressions() } returns expressions
        val expected = UiState(
            input = expressions[0],
            result = calculateResult(expressions[0]),
            outputTextColor = R.color.green
        )

        viewModel.updateConfig(isLandscape = true)
        viewModel.onEvent(UiEvents.SendToNextScreen(CommandDirection.RIGHT))

        assertEquals(expected, viewModel.uiState.first()[0])
        assertEquals(expected, viewModel.uiState.first()[1])
    }

    @Test
    fun `onEvent SendToNextScreen duplicates value in position 1 to position 0 when Direction Left`() = runTest(testDispatcher) {
        val expressions = listOf("123+34", "23434/324+2342*34.4")
        coEvery { cache.readExpressions() } returns expressions
        val expected = UiState(
                input = expressions[1],
                result = calculateResult(expressions[1]),
                outputTextColor = R.color.green
            )

        viewModel.updateConfig(isLandscape = true)
        viewModel.onEvent(UiEvents.SendToNextScreen(CommandDirection.LEFT))

        assertEquals(expected, viewModel.uiState.first()[0])
        assertEquals(expected, viewModel.uiState.first()[1])
    }


    private fun calculateResult(input: String): String {
        return Expression(input).calculate().getFormattedOutput()
    }

}