package com.example.myruns5
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

/**
 * Fragment representing the start screen where users can choose the type of input and activity
 * before beginning their tracking or data entry.
 */
class Start  : Fragment() {
    private lateinit var inputTypeSpinner: Spinner
    private lateinit var activityTypeSpinner: Spinner
    private lateinit var startButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.start, container, false)

        // initializes UI components
        inputTypeSpinner = view.findViewById(R.id.inputTypeSpinner)
        activityTypeSpinner = view.findViewById(R.id.activityTypeSpinner)
        startButton = view.findViewById(R.id.startButton)
        // sets up event handler
        setupSpinners()
        startButton.setOnClickListener { onStartButtonClick() }


        return view
    }

    //Handles the click event on the start button by starting based on the position of input
    private fun onStartButtonClick() {
        val inputTypePosition = inputTypeSpinner.selectedItemPosition  // Get the index of selected input type
        val activityTypePosition = activityTypeSpinner.selectedItemPosition
        val intent = when (inputTypeSpinner.selectedItemPosition) {
            0 -> Intent(context, Manual_input::class.java).apply {
                putExtra("INPUT_TYPE", inputTypePosition)
                putExtra("ACTIVITY_TYPE", activityTypePosition)
            }
            1, 2 -> Intent(context, Map_display::class.java).apply {
                putExtra("INPUT_TYPE", inputTypePosition)
                putExtra("ACTIVITY_TYPE", activityTypePosition)
            }
            else -> return
        }
        startActivity(intent)
    }

    // Sets up the spinners for input type and activity type using predefined arrays from resources.
    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.input_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            inputTypeSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            activityTypeSpinner.adapter = adapter
        }
    }
}