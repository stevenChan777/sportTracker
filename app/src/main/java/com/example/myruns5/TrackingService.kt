package com.example.myruns5

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.sqrt

class TrackingService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var accBuffer: ArrayBlockingQueue<Double>
    private val blockCapacity  = 64
    private val tag  = "TrackingService"
    private val _activityLabelLiveData = MutableLiveData<Int>()
    val activityLabelLiveData: LiveData<Int> get() = _activityLabelLiveData

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> get() = _locationLiveData

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "TrackingChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        accBuffer = ArrayBlockingQueue(blockCapacity  * 2)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up location callback to handle location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    _locationLiveData.postValue(location)
                }
            }
        }

        // Start the service as a foreground service
        startForegroundService()
        startLocationUpdates()
    }

    private fun startForegroundService() {
        // Create an intent to open Map_display when notification is clicked
        val intent = Intent(this, Map_display::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("MyRuns5")
            .setContentText("Recording your path now")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent) // Set the PendingIntent
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Compute magnitude
            val magnitude = sqrt(
                (event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]).toDouble()
            )

            // Add to buffer
            try {
                accBuffer.add(magnitude)

                // If the buffer is full, process the block
                if (accBuffer.size >= blockCapacity) {
                    processAccelerometerBlock()
                }
            } catch (e: IllegalStateException) {
                Log.w(tag, "Buffer full, resizing...")
                val newBuffer = ArrayBlockingQueue<Double>(accBuffer.size * 2)
                accBuffer.drainTo(newBuffer)
                accBuffer = newBuffer
                accBuffer.add(magnitude)
            }
        }
    }
    private fun processAccelerometerBlock() {
        val block = DoubleArray(blockCapacity)
        val im = DoubleArray(blockCapacity) { 0.0 }
        val featureVector = DoubleArray(blockCapacity + 1)
        var max = Double.MIN_VALUE

        for (i in block.indices) {
            block[i] = accBuffer.poll() ?: 0.0
            if (block[i] > max) {
                max = block[i]
            }
        }

        val fft = com.example.myruns5.FFT(blockCapacity)
        fft.fft(block, im)

        for (i in block.indices) {
            featureVector[i] = sqrt(block[i] * block[i] + im[i] * im[i])
        }

        featureVector[blockCapacity] = max

        classifyActivity(featureVector)
    }

    private fun classifyActivity(featureVector: DoubleArray) {
        val convertedFeatureVector = featureVector.map { it as Any }.toTypedArray()
        val activityLabel = WekaClassifier.classify(convertedFeatureVector)
        Log.d(tag, "Activity classified as: $activityLabel")
        _activityLabelLiveData.postValue(activityLabel.toInt())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): TrackingService = this@TrackingService
    }
}
