package com.example.myruns5

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.util.Calendar

/**
 * A reusable dialog fragment class that creates various types of dialogs such as date picker,
 * time picker, and generic input dialogs based on the specified type.
 */
class MyDialog : DialogFragment() {
    interface OnInputListener {
        fun onInput(dialogType: Int, input: String)
    }
    private var inputListener: OnInputListener? = null

    companion object {
        // Constants to define dialog types
        const val DIALOG_KEY = "dialog_type"
        const val DATE_DIALOG = 1
        const val TIME_DIALOG = 2
        const val DURATION_DIALOG = 3
        const val DISTANCE_DIALOG = 4
        const val CALORIES_DIALOG = 5
        const val HEART_RATE_DIALOG = 6
        const val COMMENT_DIALOG = 7
        const val UNIT_COMMENT_DIALOG = 8
        const val UNIT_PREFERENCE_DIALOG = 9

        // create a new instance of MyDialog with the specified dialog type.
        fun newInstance(dialogType: Int): MyDialog {
            val args = Bundle()
            args.putInt(DIALOG_KEY, dialogType)
            val fragment = MyDialog()
            fragment.arguments = args
            return fragment
        }
    }
// Creates the appropriate dialog based on the time and date.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogType = arguments?.getInt(DIALOG_KEY) ?: DATE_DIALOG  // Default to date dialog
        return when (dialogType) {
            DATE_DIALOG -> createDateDialog()
            TIME_DIALOG -> createTimeDialog()
            else -> createInputDialog(dialogType)
        }
    }

// Creates a DatePickerDialog initialized to the current date.
    private fun createDateDialog(): DatePickerDialog {
        val calendar = Calendar.getInstance()
        return DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "$year-${month + 1}-$dayOfMonth"
                inputListener?.onInput(DATE_DIALOG, date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

// Creates a TimePickerDialog initialized to the current time.
    private fun createTimeDialog(): TimePickerDialog {
        val clock = Calendar.getInstance()
        return TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = "$hourOfDay:$minute"
                inputListener?.onInput(TIME_DIALOG, time)
            },
            clock.get(Calendar.HOUR_OF_DAY),
            clock.get(Calendar.MINUTE),
            false
        )
    }

    //Creates an  input dialog with a custom layout for entering information
    private fun createInputDialog(dialogType: Int): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.mydialog, null)
        val editText = view.findViewById<EditText>(R.id.editTextInput)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)

        if (dialogType == DURATION_DIALOG) {
            builder.setTitle("Enter Duration")
        } else if (dialogType == DISTANCE_DIALOG) {
            builder.setTitle("Enter Distance")
        } else if (dialogType == CALORIES_DIALOG) {
            builder.setTitle("Enter Calories")
        } else if (dialogType == HEART_RATE_DIALOG) {
            builder.setTitle("Enter Heart Rate")
        } else if (dialogType == COMMENT_DIALOG) {
            builder.setTitle("Enter Comment")
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
            editText.hint = "How did it go? Notes here"
        }

        builder.setPositiveButton("OK") { dialog, which ->
            val userInput = editText.text.toString()
            inputListener?.onInput(dialogType, userInput)
            Toast.makeText(context, "Input: $userInput", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("Cancel", null)

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputListener = context as? OnInputListener
        if (inputListener == null) {
            throw ClassCastException("$context must implement OnInputListener")
        }
    }
    override fun onDetach() {
        super.onDetach()
        inputListener = null
    }

}
