package com.example.myruns5
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ViewModel to manage UI-related data for activities or fragments.
class MyViewModel: ViewModel() {
    val userImage = MutableLiveData<Bitmap>()
}