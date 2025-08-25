package com.example.myruns5
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InputEntryDao {
    @Insert
    suspend fun insert(entry: Entry)

    @Query("DELETE FROM InputEntry_table WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Query("SELECT * FROM InputEntry_table ORDER BY dateTime DESC")
    fun getAllEntries(): Flow<List<Entry>>

    @Query("SELECT * FROM InputEntry_table WHERE id = :entryId")
    fun getEntry(entryId: Long): Flow<Entry>
}