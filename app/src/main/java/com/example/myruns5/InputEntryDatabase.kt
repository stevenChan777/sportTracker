package com.example.myruns5
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters

@Database(entities = [Entry::class], version = 3)
@TypeConverters(CalendarConverter::class,LatLngConverter::class,LatLngListConverter::class)
abstract class InputEntryDatabase : RoomDatabase() {
    abstract val InputEntryDao: InputEntryDao

    companion object {
        @Volatile private var INSTANCE: InputEntryDatabase? = null

        fun getInstance(context: Context): InputEntryDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        InputEntryDatabase::class.java, "InputEntry_table"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}