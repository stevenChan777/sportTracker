package com.example.myruns5

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object LatLngConverter {
    @TypeConverter
    fun fromLatLng(latLng: LatLng): String {
        val jsonObject = JSONObject()
        jsonObject.put("latitude", latLng.latitude)
        jsonObject.put("longitude", latLng.longitude)
        return jsonObject.toString()
    }

    @TypeConverter
    fun toLatLng(json: String): LatLng {
        val jsonObject = JSONObject(json)
        return LatLng(
            jsonObject.getDouble("latitude"),
            jsonObject.getDouble("longitude")
        )
    }
}

object LatLngListConverter {
    @TypeConverter
    fun fromLatLngList(latLngList: List<LatLng>): String {
        val jsonArray = JSONArray()
        latLngList.forEach { latLng ->
            val jsonObject = JSONObject()
            jsonObject.put("latitude", latLng.latitude)
            jsonObject.put("longitude", latLng.longitude)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toLatLngList(json: String): List<LatLng> {
        val jsonArray = JSONArray(json)
        val latLngList = mutableListOf<LatLng>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val latLng = LatLng(
                jsonObject.getDouble("latitude"),
                jsonObject.getDouble("longitude")
            )
            latLngList.add(latLng)
        }
        return latLngList
    }
}
