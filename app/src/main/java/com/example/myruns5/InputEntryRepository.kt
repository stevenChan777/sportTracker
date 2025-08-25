package com.example.myruns5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InputEntryRepository(private val InputEntryDao: InputEntryDao){
    val allList: Flow<List<Entry>> = InputEntryDao.getAllEntries()

    fun insert(entry: Entry){
        CoroutineScope(IO).launch{
            InputEntryDao.insert(entry)
        }
    }
    fun deleteById(entryId: Long){
        CoroutineScope(IO).launch {
            InputEntryDao.deleteById(entryId)
        }
    }
    fun getEntry(entryId: Long): Flow<Entry> {
        return InputEntryDao.getEntry(entryId)
    }

}