package com.example.myruns5

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar


// Activity to handle manual entries for various  parameters in the MyRuns application.
class Manual_input : AppCompatActivity(), MyDialog.OnInputListener {
    private lateinit var listView: ListView

    // Buttons for saving or canceling changes
    private lateinit var manualSaveButton: Button
    private lateinit var manualCancelButton: Button
    private lateinit var viewModel: InputEntryViewModel

    private var inputType: Int = 0
    private var activityType: Int = 0
    private var duration: Double = 0.0
    private var distance: Double = 0.0
    private var calories: Double = 0.0
    private var heartRate: Double = 0.0
    private var comment: String = ""
    private var dateTime = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_entry)

        // Setup toolbar with a title
        val toolbar = findViewById<Toolbar>(R.id.myManualToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns5"

        val repository = InputEntryRepository(InputEntryDatabase.getInstance(applicationContext).InputEntryDao)
        val factory = InputEntryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(InputEntryViewModel::class.java)
        inputType = intent.getIntExtra("INPUT_TYPE", 0)
        activityType = intent.getIntExtra("ACTIVITY_TYPE", 0)

        // Initialize views from the layout
        listView = findViewById(R.id.manual_entry)
        manualSaveButton = findViewById(R.id.manualSaveButton)
        manualCancelButton = findViewById(R.id.manualCancelButton)

        // Configure list view for handling item clicks for different manual entries
        setupListView()

        // Set up click listeners for the Save and Cancel buttons
        manualSaveButton.setOnClickListener { onSaveButtonClick() }
        manualCancelButton.setOnClickListener { onCanacelButtonClick() }

    }

    /**
     * Sets up the ListView's item click listener, which triggers a dialog specific
     * to the type of manual entry selected by the user.
     */
    private fun setupListView() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val dialogId = when (position) {
                0 -> MyDialog.DATE_DIALOG
                1 -> MyDialog.TIME_DIALOG
                2 -> MyDialog.DURATION_DIALOG
                3 -> MyDialog.DISTANCE_DIALOG
                4 -> MyDialog.CALORIES_DIALOG
                5 -> MyDialog.HEART_RATE_DIALOG
                6 -> MyDialog.COMMENT_DIALOG
                else -> -1
            }
            if (dialogId != -1) {
                showInputDialog(dialogId)
            }
        }
    }
    private fun showInputDialog(dialogId: Int) {
        MyDialog.newInstance(dialogId).show(supportFragmentManager, "inputDialog")
    }

    private fun onSaveButtonClick() {

    if (!::viewModel.isInitialized) {
        Toast.makeText(this, "Error: ViewModel not initialized.", Toast.LENGTH_SHORT).show()
        return
    }
    val sharedPrefs = getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
    val unitPreference = sharedPrefs.getString("unit_preference", "Imperial") ?: "Imperial"

    val newEntry = Entry(
        inputType = inputType,
        activityType = activityType,
        dateTime =dateTime,
        duration = duration,
        distance = distance,
        calorie = calories,
        heartRate = heartRate,
        comment = comment,
        avgPace = 0.0,
        avgSpeed = 0.0,
        climb = 0.0,
        distanceUnit = unitPreference
    )
        viewModel.insert(newEntry)
        Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onCanacelButtonClick() {
        Toast.makeText(applicationContext, "Entry discarded.", Toast.LENGTH_SHORT).show()
        finish()
    }
    override fun onInput(dialogType: Int, input: String) {
        when (dialogType) {
            MyDialog.DATE_DIALOG -> {
                val parts = input.split("-")
                if (parts.size == 3) {
                    dateTime.set(Calendar.YEAR, parts[0].toInt())
                    dateTime.set(Calendar.MONTH, parts[1].toInt() - 1)
                    dateTime.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            }
            MyDialog.TIME_DIALOG -> {
                val parts = input.split(":")
                if (parts.size == 2) {
                    dateTime.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    dateTime.set(Calendar.MINUTE, parts[1].toInt())
                }
            }
            MyDialog.DURATION_DIALOG -> {
                duration = input.toDoubleOrNull() ?: 0.0
            }
            MyDialog.DISTANCE_DIALOG -> {
                distance = input.toDoubleOrNull() ?: 0.0
            }
            MyDialog.CALORIES_DIALOG -> {
                calories = input.toDoubleOrNull() ?: 0.0
            }
            MyDialog.HEART_RATE_DIALOG -> {
                heartRate = input.toDoubleOrNull() ?: 0.0
            }
            MyDialog.COMMENT_DIALOG -> {
                comment = input
            }
        }
    }
}
