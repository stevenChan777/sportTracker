package com.example.myruns5
import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import kotlinx.coroutines.Dispatchers.IO

class InputEntryViewModel(private val repository: InputEntryRepository) : ViewModel() {
    val allLiveData: LiveData<List<Entry>> = repository.allList.asLiveData()
    fun insert(entry: Entry) {
        CoroutineScope(IO).launch{
            repository.insert(entry)
        }
    }

    fun deleteById(entryId: Long) {
        CoroutineScope(IO).launch{
            repository.deleteById(entryId)
        }
    }
    fun getEntry(entryId: Long): LiveData<Entry> {
        return repository.getEntry(entryId).asLiveData()
    }
}

class InputEntryViewModelFactory(private val repository: InputEntryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InputEntryViewModel::class.java)) {
            return InputEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}