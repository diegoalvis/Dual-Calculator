package com.diegoalvis.dualcalculator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.diegoalvis.dualcalculator.databinding.FragmentCalculatorBinding
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class CalculatorFragment : Fragment(R.layout.fragment_calculator) {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var _binding: FragmentCalculatorBinding
    private val screenPosition: Int by lazy { requireNotNull(arguments?.getInt(SCREEN_POS)) }

    companion object {
        private const val SCREEN_POS = "screenPos"
        fun newInstance(screenPos: Int): CalculatorFragment {
            return CalculatorFragment().apply {
                arguments = Bundle().apply {
                    putInt(SCREEN_POS, screenPos)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(_binding) {
            buttonClear.setOnClickListener {
                input.text = ""
                output.text = ""
                onCalculate()
            }
            buttonBracketStart.setOnClickListener {
                input.addToInputText("(")
            }
            buttonBracketEnd.setOnClickListener {
                input.addToInputText(")")
            }
            buttonCroxx.setOnClickListener {
                val removedLast = input.text.toString().dropLast(1)
                input.text = removedLast
            }
            button0.setOnClickListener {
                input.addToInputText("0")
            }
            button1.setOnClickListener {
                input.addToInputText("1")
            }
            button2.setOnClickListener {
                input.addToInputText("2")
            }
            button3.setOnClickListener {
                input.addToInputText("3")
            }
            button4.setOnClickListener {
                input.addToInputText("4")
            }
            button5.setOnClickListener {
                input.addToInputText("5")
            }
            button6.setOnClickListener {
                input.addToInputText("6")
            }
            button7.setOnClickListener {
                input.addToInputText("7")
            }
            button8.setOnClickListener {
                input.addToInputText("8")
            }
            button9.setOnClickListener {
                input.addToInputText("9")
            }
            buttonDot.setOnClickListener {
                input.addToInputText(".")
            }
            buttonDivision.setOnClickListener {
                input.addToInputText("÷") // ALT + 0247
            }
            buttonMultiply.setOnClickListener {
                input.addToInputText("×") // ALT + 0215
            }
            buttonSubtraction.setOnClickListener {
                input.addToInputText("-")
            }
            buttonAddition.setOnClickListener {
                input.addToInputText("+")
            }
            buttonEquals.setOnClickListener {
                onCalculate()
            }
        }

        with(viewLifecycleOwner) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect { list ->
                        val uiState = list[screenPosition]
                        showResult(uiState = uiState)
                    }
                }
            }
        }
    }

    private fun onCalculate() {
        viewModel.onEvent(
            UiEvents.Calculate(
                screenPosition = screenPosition,
                input = _binding.input.text.toString()
            )
        )
    }

    private fun TextView.addToInputText(buttonValue: String) {
        text = text.toString() + "" + buttonValue
    }

    private fun showResult(uiState: UiState) {
        with(_binding) {
            output.setTextColor(ContextCompat.getColor(requireContext(), uiState.outputTextColor))
            input.text = uiState.input
            output.text = uiState.result
        }
    }
}