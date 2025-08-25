package com.example.myruns5

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

/**
 * Fragment for managing application settings. It displays lists of different settings
 * categories and provides interaction mechanisms for each setting.
 */
class Settings : Fragment() {
    private lateinit var preferenceListView: ListView
    private lateinit var additionalSettingsListView: ListView
    private lateinit var miscListView: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings, container, false)
        setupDefaultUnitPreference()
        setupListViews(view)
        return view
    }

    // Initializes the ListView widgets and sets up their adapters with the appropriate data.
    private fun setupListViews(view: View) {
        preferenceListView = view.findViewById(R.id.preferenceListView) as ListView
        additionalSettingsListView = view.findViewById(R.id.additionalSettingsListView) as ListView
        miscListView = view.findViewById(R.id.miscListView) as ListView

        val preferenceItems = listOf(
            SettingItem("User Profile", "Name, Email, Class, etc.", false),
            SettingItem("Privacy Setting", "Posting your records anonymously", true)
        )
        val additionalItems = listOf(
            SettingItem("Unit Preference", "Select the units", false),
            SettingItem("Comments", "Please enter your comments", false)
        )
        val miscItems = listOf(
            SettingItem("Webpage", "http://www.sfu.ca/computing.html", false)
        )

        preferenceListView.adapter = SettingsAdapter(requireContext(), preferenceItems)
        additionalSettingsListView.adapter = SettingsAdapter(requireContext(), additionalItems)
        miscListView.adapter = SettingsAdapter(requireContext(), miscItems)

        setupPreferenceViewClickListener()
        setupMiscListViewClickListener()
        setupAdditionalSettingsListViewClickListener()
    }
   // Configures click listeners for preference items, linking it to profile page.
    private fun setupPreferenceViewClickListener() {
        preferenceListView.setOnItemClickListener { _, _, position, _ ->
            val item = preferenceListView.adapter.getItem(position) as SettingItem
            if (item.title == "User Profile") {
                val intent = Intent(context, Profile::class.java)
                startActivity(intent)
            }

        }
    }
    // Configures click listeners for misc items, linking it to the webpage.
    private fun setupMiscListViewClickListener() {
        miscListView.setOnItemClickListener { _, _, position, _ ->
            val item = miscListView.adapter.getItem(position) as SettingItem
            if (item.title == "Webpage") {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(item.subTitle)
                startActivity(intent)
            }
        }
    }
    // Configures click listeners for additional settings items, linking it to the different alert dialog.
    private fun setupAdditionalSettingsListViewClickListener() {
        additionalSettingsListView.setOnItemClickListener { _, _, position, _ ->
            val item = additionalSettingsListView.adapter.getItem(position) as SettingItem
            when (item.title) {
                "Unit Preference" -> showUnitPreferenceDialog()
                "Comments" -> showCommentsDialog()
            }
        }
    }

    // Displays a dialog for selecting unit preferences.
    private fun showUnitPreferenceDialog() {
        val units = arrayOf("Metric (Kilometers)", "Imperial (Miles)")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Unit Preference")
            .setItems(units) { _, which ->
                val unitPreference = if (which == 0) "Metric" else "Imperial"
                val sharedPrefs = requireContext().getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("unit_preference", unitPreference)
                    apply()
                }
                Toast.makeText(context, "Selected: $unitPreference", Toast.LENGTH_SHORT).show()
            }
        builder.setNegativeButton("Cancel", null)
        builder.create().show()
    }

    // Displays a dialog for entering comments.
    private fun showCommentsDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Comments")
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, which ->
            val comment = input.text.toString()
            Toast.makeText(context, "Comment: $comment", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.create().show()
    }

    private fun setupDefaultUnitPreference() {
        val sharedPrefs = requireContext().getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
        if (!sharedPrefs.contains("unit_preference")) {
            with(sharedPrefs.edit()) {
                putString("unit_preference", "Metric")
                apply()
            }
        }
    }
}


