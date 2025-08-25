package com.example.myruns5

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import java.util.*

@Entity(tableName = "InputEntry_table")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var inputType: Int,
    var activityType: Int,
    @TypeConverters(CalendarConverter::class) val dateTime: Calendar,
    var duration: Double,
    var distance: Double,
    var avgPace: Double,
    var avgSpeed: Double,
    var calorie: Double,
    var climb: Double,
    var heartRate: Double,
    var comment: String,
    var distanceUnit: String = "Metric",
    val pathPoints: List<LatLng> = emptyList()
)

class CalendarConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    @TypeConverter
    fun dateToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }
}
