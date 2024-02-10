package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.model.Diary
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.util.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarViewModel(application: Application) : CoroutineViewModel(application) {

    val calendarDiaryView = MutableLiveData<List<Diary>?>()
    val calendarNotesErrorMessage = MutableLiveData<Boolean>()
    val calendarNoteNullMessage = MutableLiveData<Boolean>()
    val calendarToastMessage = MutableLiveData<String?>()

    private val getService = GetService()
    private val sharedPreferences = SharedPreferences(getApplication())


    fun saveStringData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun refreshCalendarFragment(noteDate : String){
        calendarDiaryView.value = null
        calendarToastMessage.value = null
        calendarNoteNullMessage.value = false
        calendarNotesErrorMessage.value = false
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
                        calendarNoteNullMessage.value = true
                    } else {
                        calendarDiaryView.value = notes
                    }
                }

            } catch (error: Exception) {
                calendarNotesErrorMessage.value = true
                withContext(Dispatchers.Main) {
                    calendarToastMessage.value = error.localizedMessage
                }
            }
        }
    }


}
