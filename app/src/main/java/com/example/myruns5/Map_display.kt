package com.example.myruns5

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import android.graphics.Color
import java.util.Calendar

class Map_display : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var isMapReady = false
    private var isInitialZoomDone = false

    private var trackingService: TrackingService? = null
    private var isServiceBound = false

    private var currentLocation: Location? = null
    private var pathPoints = mutableListOf<LatLng>()
    private lateinit var mapSaveButton: Button
    private lateinit var mapCancelButton: Button
    private var startMarker: Marker? = null
    private var currentMarker: Marker? = null

    // UI elements for displaying stats
    private lateinit var typeText: TextView
    private lateinit var avgSpeedText: TextView
    private lateinit var curSpeedText: TextView
    private lateinit var climbText: TextView
    private lateinit var calorieText: TextView
    private lateinit var distanceText: TextView

    private var inputType: Int = 0
    private var activityType: Int = 0
    private var totalDistance = 0.0
    private var totalCalories = 0.0
    private var totalClimb = 0.0
    private var avgSpeed = 0.0
    private var lastLocation: Location? = null
    private lateinit var viewModel: InputEntryViewModel
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener
    private val classifierToActivityTypeMap = arrayOf(-1,2, 1, 0)
    private var classifiedIndex = -1;
    private var startTime: Long = 0 // Start time in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_display)
        startTime = System.currentTimeMillis()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.myMapToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Map"

        val repository = InputEntryRepository(InputEntryDatabase.getInstance(applicationContext).InputEntryDao)
        val factory = InputEntryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(InputEntryViewModel::class.java)
        inputType = intent.getIntExtra("INPUT_TYPE", 0)
        activityType = intent.getIntExtra("ACTIVITY_TYPE", 0)

        sharedPrefs = getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
        setupPreferenceChangeListener()

        // Initialize buttons and stats TextViews from the layout
        mapSaveButton = findViewById(R.id.mapSaveButton)
        mapCancelButton = findViewById(R.id.mapCancelButton)
        typeText = findViewById(R.id.typeText)
        avgSpeedText = findViewById(R.id.avgSpeedText)
        curSpeedText = findViewById(R.id.curSpeedText)
        climbText = findViewById(R.id.climbText)
        calorieText = findViewById(R.id.calorieText)
        distanceText = findViewById(R.id.distanceText)

        // Set click listeners for save and cancel actions
        mapSaveButton.setOnClickListener { onSaveButtonClick() }
        mapCancelButton.setOnClickListener { onCancelButtonClick() }

        // Initialize the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Start and bind the tracking service
        startTrackingService()
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.getService()
            isServiceBound = true
            observeLocationUpdates()
            observeActivityUpdates()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            isServiceBound = false
        }
    }

    private fun observeLocationUpdates() {
        trackingService?.locationLiveData?.observe(this, Observer { location ->
            onLocationUpdate(location)
        })
    }
    private fun observeActivityUpdates() {
        trackingService?.activityLabelLiveData?.observe(this) { activityLabel ->
            updateActivityTypeUI(activityLabel)
        }
    }
    private fun updateActivityTypeUI(activityLabel: Int) {
        val activityNames = resources.getStringArray(R.array.activity_types)

        if (inputType == 1) {
            if (activityType in activityNames.indices) {
                typeText.text = "Type: ${activityNames[activityType]}"
            } else {
                typeText.text = "Type: Unknown"
            }
        } else if (inputType == 2) {
            val mappedActivityIndex = if (activityLabel in classifierToActivityTypeMap.indices) {
                classifierToActivityTypeMap[activityLabel]
            } else {
                -1
            }
            typeText.text = if (mappedActivityIndex in activityNames.indices) {
                "Type: ${activityNames[mappedActivityIndex]}"
            } else {
                "Type: Unknown"
            }
            classifiedIndex = mappedActivityIndex
        }
    }
    private fun onLocationUpdate(location: Location) {
        currentLocation = location
        updateMapLocation()
    }

    private fun updateMapLocation() {
        if (isMapReady && ::mMap.isInitialized) {
            currentLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                pathPoints.add(latLng)
                val unitPreference = sharedPrefs.getString("unit_preference", "Metric") ?: "Metric"
                val isMetric = unitPreference == "Metric"

                // Calculate distance in kilometers
                lastLocation?.let { lastLoc ->
                    val distance = if (isMetric) lastLoc.distanceTo(location) / 1000.0 else lastLoc.distanceTo(location) / 1609.34
                    totalDistance += distance
                    distanceText.text = "Distance: %.2f ${if (isMetric) "km" else "miles"}".format(totalDistance)

                    // Calculate climb (difference in altitude)
                    val altitudeDifference = location.altitude - lastLoc.altitude
                    if (altitudeDifference > 0) {
                        val climb = if (isMetric) altitudeDifference else altitudeDifference * 3.28084
                        totalClimb += climb
                        climbText.text = "Climb: %.2f ${if (isMetric) "m" else "ft"}".format(totalClimb)
                    }else{
                        climbText.text = "Climb: 0 ${if (isMetric) "m" else "ft"}".format(totalClimb)
                    }

                    // Calculate calories based on speed, distance, and climb
                    val speedKmh = location.speed * 3.6 // Convert m/s to km/h
                    val intensityFactor = 1.0 + (speedKmh / 5.0) + (totalClimb / 50.0)
                    totalCalories += (distance * 1000 * 0.1 * intensityFactor) // Convert distance back to meters for calculation
                    calorieText.text = "Calories: %.2f".format(totalCalories)
                }
                lastLocation = location

                // Update current speed in km/h
                val speed = if (isMetric) location.speed * 3.6 else location.speed * 2.23694
                curSpeedText.text = "Cur Speed: %.2f ${if (isMetric) "km/h" else "mph"}".format(speed)

                // Update average speed in km/h
                if (totalDistance > 0) {
                    avgSpeed = (totalDistance / ((System.currentTimeMillis() - startTime) / 1000.0)) * (if (isMetric) 3600 else 2237)
                    avgSpeedText.text = "Avg Speed: %.2f ${if (isMetric) "km/h" else "mph"}".format(avgSpeed)
                }

                // Draw polyline for the path
                mMap.addPolyline(PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(5f))

                // Set the starting point marker only once
                if (startMarker == null) {
                    startMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Starting Point")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                }

                // Update current location marker
                currentMarker?.remove()
                currentMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                // Zoom to current location only initially
                if (!isInitialZoomDone) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    isInitialZoomDone = true
                }
            }
        }
    }

    private fun setupPreferenceChangeListener() {
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "unit_preference") {
                // Update UI based on preference change
                updateMapLocation()
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
    private fun onSaveButtonClick() {
        val sharedPrefs = getSharedPreferences("MyRunsPreferences", Context.MODE_PRIVATE)
        val unitPreference = sharedPrefs.getString("unit_preference", "Metric") ?: "Metric"
        val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val resolvedActivityType = if (inputType == 1) {
            activityType
        } else if (inputType == 2) {
            classifiedIndex
        } else {
            -1
        }
        android.util.Log.d("MapDisplayActivity", "activityType:$activityType Resolved Activity Type: $resolvedActivityType")

        val entry = Entry(
            inputType = inputType,
            activityType = resolvedActivityType,
            dateTime = Calendar.getInstance(),
            duration = durationSeconds.toDouble(),
            distance = totalDistance,
            calorie = totalCalories,
            heartRate = 0.0,
            comment = "",
            avgPace = 0.0,
            avgSpeed = avgSpeed,
            climb = totalClimb,
            distanceUnit = unitPreference,
            pathPoints = pathPoints.toList()
        )

        viewModel.insert(entry)
        Toast.makeText(applicationContext, "Workout saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onCancelButtonClick() {
        Toast.makeText(applicationContext, "Entry discarded.", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true
        updateMapLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}
