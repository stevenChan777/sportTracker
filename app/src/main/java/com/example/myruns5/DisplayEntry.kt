package com.example.myruns5

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DisplayEntry : AppCompatActivity() {

    private lateinit var viewModel: InputEntryViewModel
    private var entryId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_entry)
        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns5"

        entryId = intent.getLongExtra("ENTRY_ID", -1)

        val repository = InputEntryRepository(InputEntryDatabase.getInstance(applicationContext).InputEntryDao)
        val factory = InputEntryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(InputEntryViewModel::class.java)

        val inputTypeTextView: TextView = findViewById(R.id.inputTypeTextView)
        val activityTypeTextView: TextView = findViewById(R.id.activityTypeTextView)
        val dateTimeTextView: TextView = findViewById(R.id.dateTimeTextView)
        val durationTextView: TextView = findViewById(R.id.durationTextView)
        val distanceTextView: TextView = findViewById(R.id.distanceTextView)
        val caloriesTextView: TextView = findViewById(R.id.caloriesTextView)
        val heartRateTextView: TextView = findViewById(R.id.heartRateTextView)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        val sharedPrefs = getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
        val unitPreference = sharedPrefs.getString("unit_preference", "Metric")

        viewModel.allLiveData.observe(this) { entries ->
            val entry = entries.find { it.id == entryId }
            entry?.let {
                val activityNames = resources.getStringArray(R.array.activity_types)
                val inputNames = resources.getStringArray(R.array.input_types)

                val activityName = if (it.activityType in activityNames.indices) {
                    activityNames[it.activityType]
                } else {
                    "Unknown Activity"
                }

                val inputTypeName = if (it.inputType in inputNames.indices) {
                    inputNames[it.inputType]
                } else {
                    "Unknown Input Type"
                }

                val dateFormatted = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
                ).format(it.dateTime.time)

                inputTypeTextView.text = inputTypeName
                activityTypeTextView.text = activityName
                dateTimeTextView.text = dateFormatted
                val minutes = (it.duration).toInt()
                val seconds = ((it.duration - minutes) * 60).toInt()
                durationTextView.text = "$minutes mins $seconds secs"

                val distance = if (unitPreference == "Metric") {
                    if (it.distanceUnit == "Imperial") {
                        "${String.format("%.4f", Util.milesToKilometers(it.distance))} km"
                    } else {
                        "${it.distance} km"
                    }
                } else {
                    if (it.distanceUnit == "Metric") {
                        "${String.format("%.4f", Util.kilometersToMiles(it.distance))} miles"
                    } else {
                        "${it.distance} miles"
                    }
                }
                distanceTextView.text = distance

                caloriesTextView.text = "${it.calorie} cals"
                heartRateTextView.text = "${it.heartRate} bpm"
            }
        }

        // Set up delete button click listener
        deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteById(entryId)
                runOnUiThread {
                    Toast.makeText(this@DisplayEntry, "Entry deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
