package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.model.Diary
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.util.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarViewModel(application: Application) : CoroutineViewModel(application) {

    private val _diaryView = MutableLiveData<List<Diary>?>()
    val diaryView : LiveData<List<Diary>?>
        get() = _diaryView
    private val _errorMessage = MutableLiveData<Boolean>()
    val errorMessage : LiveData<Boolean>
        get() = _errorMessage
    private val _nullMessage = MutableLiveData<Boolean>()
    val nullMessage : LiveData<Boolean>
        get() = _nullMessage
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage : LiveData<String?>
        get() = _toastMessage

    private val getService = GetService()
    private val sharedPreferences = SharedPreferences(getApplication())

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun refreshCalendarFragment(noteDate : String){
        _diaryView.value = null
        _toastMessage.value = null
        _nullMessage.value = false
        _errorMessage.value = false
        getDiariesForSelectedDate(noteDate)
    }

     fun getDiariesForSelectedDate(noteDate: String) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    getService.getCalendarNotesFromFirebase(noteDate)
                }
                val result = getService.getCalendarNotesFromFirebase(noteDate)
                if (result.isSuccess) {
                    val notes = result.getOrNull()
                    if (notes.isNullOrEmpty()) {
                        _nullMessage.value = true
                    } else {
                        _diaryView.value = notes
                    }
                }
            } catch (error: Exception) {
                _errorMessage.value = true
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
                }
            }
        }
    }
}
