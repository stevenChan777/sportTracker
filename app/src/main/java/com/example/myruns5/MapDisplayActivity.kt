package com.example.myruns5

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var isMapReady = false

    private lateinit var viewModel: InputEntryViewModel
    private var entryId: Long = -1
    private var entry: Entry? = null

    // UI elements for displaying stats
    private lateinit var typeText: TextView
    private lateinit var avgSpeedText: TextView
    private lateinit var curSpeedText: TextView
    private lateinit var climbText: TextView
    private lateinit var calorieText: TextView
    private lateinit var distanceText: TextView
    private lateinit var deleteButton: Button

    private var startMarker: Marker? = null
    private var currentMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_display)

        deleteButton = findViewById(R.id.deleteButton)

        // Retrieve ENTRY_ID from intent
        entryId = intent.getLongExtra("ENTRY_ID", -1)
        if (entryId == -1L) {
            Toast.makeText(this, "Invalid entry ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI elements
        typeText = findViewById(R.id.typeText)
        avgSpeedText = findViewById(R.id.avgSpeedText)
        curSpeedText = findViewById(R.id.curSpeedText)
        climbText = findViewById(R.id.climbText)
        calorieText = findViewById(R.id.calorieText)
        distanceText = findViewById(R.id.distanceText)

        // Set up ViewModel and fetch entry
        val repository = InputEntryRepository(
            InputEntryDatabase.getInstance(applicationContext).InputEntryDao
        )
        val factory = InputEntryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(InputEntryViewModel::class.java)

        viewModel.getEntry(entryId).observe(this) { fetchedEntry ->
            if (fetchedEntry != null) {
                entry = fetchedEntry
                displayEntryDetails(fetchedEntry)
            } else {
                Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        deleteButton.setOnClickListener { onDeleteButtonClick() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true

        entry?.let {
            displayEntryOnMap(it)
        }
    }

    private fun displayEntryDetails(entry: Entry) {
        val sharedPrefs = getSharedPreferences("MyRunsPreferences", MODE_PRIVATE)
        val unitPreference = sharedPrefs.getString("unit_preference", "Metric") ?: "Metric"
        val isMetric = unitPreference == "Metric"

        typeText.text = "Type: ${getActivityTypeName(entry.activityType)}"

        // Apply unit conversion logic
        val entryDistanceUnit = entry.distanceUnit
        if (entryDistanceUnit == "Imperial" && isMetric) {
            distanceText.text = "Distance: %.2f km".format(entry.distance * 1.60934)
            avgSpeedText.text = "Avg Speed: %.2f km/h".format(entry.avgSpeed * 1.60934)
            climbText.text = "Climb: %.2f m".format(entry.climb / 3.28084)
        } else if (entryDistanceUnit == "Metric" && isMetric) {
            distanceText.text = "Distance: %.2f km".format(entry.distance)
            avgSpeedText.text = "Avg Speed: %.2f km/h".format(entry.avgSpeed)
            climbText.text = "Climb: %.2f m".format(entry.climb)
        } else if (entryDistanceUnit == "Metric" && !isMetric) {
            distanceText.text = "Distance: %.2f miles".format(entry.distance / 1.60934)
            avgSpeedText.text = "Avg Speed: %.2f mph".format(entry.avgSpeed / 1.60934)
            climbText.text = "Climb: %.2f ft".format(entry.climb * 3.28084)
        } else if (entryDistanceUnit == "Imperial" && !isMetric) {
            distanceText.text = "Distance: %.2f miles".format(entry.distance)
            avgSpeedText.text = "Avg Speed: %.2f mph".format(entry.avgSpeed)
            climbText.text = "Climb: %.2f ft".format(entry.climb)
        }

        calorieText.text = "Calories: %.2f".format(entry.calorie)
        curSpeedText.text = "Cur Speed: n/a"

        // Display path points if map is ready
        if (isMapReady) {
            displayEntryOnMap(entry)
        }
    }

    private fun displayEntryOnMap(entry: Entry) {
        if (entry.pathPoints.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .addAll(entry.pathPoints)
                .width(5f)
                .color(Color.BLUE)
            mMap.addPolyline(polylineOptions)

            val firstPoint = entry.pathPoints.first()
            startMarker?.remove()
            startMarker = mMap.addMarker(
                MarkerOptions()
                    .position(firstPoint)
                    .title("Starting Point")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )

            val lastPoint = entry.pathPoints.last()
            currentMarker?.remove()
            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(lastPoint)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 15f))
        }
    }

    private fun getActivityTypeName(activityType: Int): String {
        val activityNames = resources.getStringArray(R.array.activity_types)
        return if (activityType in activityNames.indices) activityNames[activityType] else "Unknown"
    }

    private fun onDeleteButtonClick() {
        entryId.takeIf { it != -1L }?.let { id ->
            viewModel.deleteById(id)
            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            finish()
        } ?: run {
            Toast.makeText(this, "Invalid entry ID", Toast.LENGTH_SHORT).show()
        }
    }
}
