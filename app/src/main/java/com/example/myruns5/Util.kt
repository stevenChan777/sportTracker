package com.example.myruns5
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility class providing helper functions related to permissions and image processing.
 */
object Util {
    const val PERMISSION_REQUEST_CODE = 1001

    // List of required permissions for the app
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.CAMERA

    )

    // Function to check if all permissions are granted
    fun hasAllPermissions(activity: Activity): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Function to request permissions
    fun requestPermissions(activity: Activity) {
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Function to handle permission request results
    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionsGranted: () -> Unit
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionsGranted()
            }
        }
    }

    // Creates and returns a bitmap from a specified Uri after applying a rotation transformation.
    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }

    fun milesToKilometers(miles: Double): Double = String.format("%.4f", miles * 1.60934).toDouble()
    fun kilometersToMiles(kilometers: Double): Double = String.format("%.4f", kilometers / 1.60934).toDouble()
}