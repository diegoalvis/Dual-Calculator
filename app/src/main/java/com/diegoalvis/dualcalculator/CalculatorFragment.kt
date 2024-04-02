package com.diegoalvis.dualcalculator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.diegoalvis.dualcalculator.databinding.FragmentCalculatorBinding
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

@SuppressLint("SetTextI18n")
class CalculatorFragment : Fragment(R.layout.fragment_calculator) {

    private lateinit var _binding: FragmentCalculatorBinding

    companion object {
        fun newInstance() = CalculatorFragment()
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
                showResult()
            }
        }
    }

    private fun TextView.addToInputText(buttonValue: String) {
        text = text.toString() + "" + buttonValue
    }

    private fun getInputExpression(): String {
        var expression = _binding.input.text.replace(Regex("÷"), "/")
        expression = expression.replace(Regex("×"), "*")
        return expression
    }

    private fun showResult() {
        with(_binding) {
            try {
                val expression = getInputExpression()
                val result = Expression(expression).calculate()
                if (result.isNaN()) {
                    // Show Error Message
                    output.text = ""
                    output.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                } else {
                    // Show Result
                    output.text = DecimalFormat("0.######").format(result).toString()
                    output.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                }
            } catch (e: Exception) {
                // Show Error Message
                output.text = ""
                output.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
        }
    }
}