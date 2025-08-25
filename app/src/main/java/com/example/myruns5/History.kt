package com.example.myruns5

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

class History : Fragment() {

    private lateinit var viewModel: InputEntryViewModel
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.history, container, false)
        listView = view.findViewById(R.id.HistoryListView)
        sharedPrefs = requireContext().getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
        setupPreferenceChangeListener()

        // Initialize the repository and ViewModel with a factory
        val repository = InputEntryRepository(InputEntryDatabase.getInstance(requireContext()).InputEntryDao)
        val factory = InputEntryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(InputEntryViewModel::class.java)
        viewModel.allLiveData.observe(viewLifecycleOwner) { entries ->
            loadHistoryList(entries)
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = viewModel.allLiveData.value?.get(position)
            entry?.let {
                val context = requireContext() // Get the context explicitly
                val intent = if (it.inputType == 0) {
                    Intent(context, DisplayEntry::class.java) // For inputType 0
                } else {
                    Intent(context, MapDisplayActivity::class.java) // For inputType 1 or 2
                }
                intent.putExtra("ENTRY_ID", it.id)
                startActivity(intent)
            }
        }
        return view
    }

    private fun loadHistoryList(entries: List<Entry>) {
        val data = entries.map { entry ->
            val activityNames = resources.getStringArray(R.array.activity_types)
            val inputNames = resources.getStringArray(R.array.input_types)
            val sharedPrefs = requireContext().getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
            val unitPreference = sharedPrefs.getString("unit_preference", "Metric")

            val activityName = if (entry.activityType in activityNames.indices) {
                activityNames[entry.activityType]
            } else {
                "Unknown"
            }
            val inputTypeName = if (entry.inputType in inputNames.indices) {
                inputNames[entry.inputType]
            } else {
                "Unknown Input Type"
            }
            val dateFormatted = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(entry.dateTime.time)
            val distance = if (unitPreference == "Metric") {
                if (entry.distanceUnit == "Imperial") {
                    "${Util.milesToKilometers(entry.distance)} km"
                } else {
                    "${entry.distance} km"
                }
            } else {
                if (entry.distanceUnit == "Metric") {
                    "${Util.kilometersToMiles(entry.distance)} miles"
                } else {
                    "${entry.distance} miles"
                }
            }
            val duration = if ((entry.duration).toInt() >= 60) {
                val minutes = (entry.duration).toInt() / 60
                val seconds = (entry.duration).toInt() % 60
                "$minutes mins $seconds secs"
            } else {
                val seconds = (entry.duration).toInt() % 60
                "$seconds secs"
            }

            mapOf(
                "title" to "$inputTypeName: $activityName, $dateFormatted",
                "subtitle" to "$distance, $duration"
            )
        }

        val adapter = SimpleAdapter(
            requireContext(),
            data,
            android.R.layout.simple_list_item_2,
            arrayOf("title", "subtitle"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        listView.adapter = adapter
    }

    private fun setupPreferenceChangeListener() {
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "unit_preference") {
                viewModel.allLiveData.value?.let { loadHistoryList(it) }
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}
