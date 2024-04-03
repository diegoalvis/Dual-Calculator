package com.diegoalvis.dualcalculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import com.diegoalvis.dualcalculator.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    companion object {
        const val FIRST_FRAGMENT_POS = 0
        const val SECOND_FRAGMENT_POS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        val isLandscape = _binding.fragmentContainerB != null // other way: use Configuration.ORIENTATION_LANDSCAPE
        viewModel.initialSetup(isLandscape)

        supportFragmentManager.commit {
            replace(R.id.fragment_container, CalculatorFragment.newInstance(FIRST_FRAGMENT_POS), "$FIRST_FRAGMENT_POS")
            if (isLandscape) {
                println("Landscape")
                replace(R.id.fragment_container_b, CalculatorFragment.newInstance(SECOND_FRAGMENT_POS), "$SECOND_FRAGMENT_POS")
            }
            setReorderingAllowed(true)
        }


        _binding.switchLeft?.setOnClickListener {
            viewModel.reduce(UiEvents.SendToNextScreen(direction = CommandDirection.LEFT))
        }
        _binding.switchRight?.setOnClickListener {
            viewModel.reduce(UiEvents.SendToNextScreen(direction = CommandDirection.RIGHT))
        }

    }
}