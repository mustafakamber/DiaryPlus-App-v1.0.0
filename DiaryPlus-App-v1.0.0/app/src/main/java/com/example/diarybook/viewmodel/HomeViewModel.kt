package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.model.Diary
import com.example.diarybook.service.AuthService
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.MoveService
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.view.dialog.DatePickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel (application: Application) : CoroutineViewModel(application) {

    val homeDiaryView = MutableLiveData<List<Diary>?>()
    val homeCalendarView = MutableLiveData<ArrayList<Int>?>()
    val homeErrorMessage = MutableLiveData<Boolean>()
    val homeNullMessage = MutableLiveData<Boolean>()
    val homeSearchView = MutableLiveData<Boolean>()
    val homeUserName = MutableLiveData<String?>()
    val homeToastMessage = MutableLiveData<String?>()
    val homeSnackbarMessage = MutableLiveData<Int>()
    val homeDateText = MutableLiveData<String?>()

    private val getService = GetService()
    private val authService = AuthService()
    private val moveService = MoveService()
    private val deleteService = DeleteService()

    private val sharedPreferences = SharedPreferences(getApplication())
    
    val datePicker = DatePickerDialog(getApplication())


    fun saveStringData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }


    fun refreshHomeFragment() {
        homeUserName.value = null
        homeDateText.value = null
        homeDiaryView.value = null
        homeCalendarView.value = null
        homeToastMessage.value = null
        homeErrorMessage.value = false
        homeNullMessage.value = false
        homeSearchView.value = false
        getUserName()
        getDate()
        getImage()
        getAllDiaries()
    }

     fun getAllDiaries(){
        launch {
            try {
                withContext(Dispatchers.IO) {
                    getService.getAllNotesFromFirebase()
                }
                val result = getService.getAllNotesFromFirebase()
                if (result.isSuccess) {
                    val notes = result.getOrNull()
                    if (notes.isNullOrEmpty()) {
                        homeNullMessage.value = true
                    } else {
                        homeDiaryView.value = notes
                    }
                }

            } catch (error: Exception) {
                homeErrorMessage.value = true
                withContext(Dispatchers.Main) {
                    homeToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private fun getDate(){

        val date = datePicker.currentDate()

        homeDateText.value = date
    }


    private fun getImage(){

        val month = datePicker.currentMonth()

        val calendarPhoto : Array<Int> = arrayOf(
            R.drawable.base_calendar_january, R.drawable.base_calendar_february, R.drawable.base_calendar_march,
            R.drawable.base_calendar_april, R.drawable.base_calendar_may, R.drawable.base_calendar_june,
            R.drawable.base_calendar_july, R.drawable.base_calendar_august,
            R.drawable.base_calendar_september, R.drawable.base_calendar_october,
            R.drawable.base_calendar_november,R.drawable.base_calendar_december
        )

        val indexOfMonth = when (month) {
            "January" -> 0
            "February" -> 1
            "March" -> 2
            "April" -> 3
            "May" -> 4
            "June" -> 5
            "July" -> 6
            "August" -> 7
            "September" -> 8
            "October" -> 9
            "November" -> 10
            "December" -> 11
            else -> -1
        }

        if (indexOfMonth != -1) {

            val newList: MutableList<Int> = mutableListOf()
            for (i in indexOfMonth until calendarPhoto.size) {
                newList.add(calendarPhoto[i])
            }
            for (i in 0 until indexOfMonth) {
                newList.add(calendarPhoto[i])
            }

            val newPhotoList = newList.toTypedArray()
            val newPhotoArrayList = ArrayList<Int>(newPhotoList.asList())

            homeCalendarView.value = newPhotoArrayList

        }

    }

    private fun getUserName(){
        launch {
            try {
                withContext(Dispatchers.IO){
                    authService.getUserInfo()
                }
                val result = authService.getUserInfo()
                if (result.isSuccess){
                    val userName = result.getOrNull()

                    userName?.let {
                        homeUserName.value = userName.userName!!.substringBefore(" ")
                    }
                }
            }catch (error : Exception){
                withContext(Dispatchers.Main){
                    homeToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun deleteNote(noteId: String) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    deleteService.deleteSingleNoteFromFirebase(noteId)
                }
                homeSnackbarMessage.value = R.string.deleted
                getAllDiaries()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    homeToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun moveNoteToArchive(noteId: String) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    moveService.moveSingleNoteToArchiveFirebase(noteId)
                }
                homeSnackbarMessage.value = R.string.archived
                getAllDiaries()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    homeToastMessage.value = error.localizedMessage
                }
            }
        }
    }



}