package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.model.Calendar
import com.example.diarybook.model.Diary
import com.example.diarybook.service.AuthService
import com.example.diarybook.service.CalendarService
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.MoveService
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.view.dialog.DatePickerDialog
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel (application: Application) : CoroutineViewModel(application) {

    val homeDiaryView = MutableLiveData<List<Diary>?>()
    val homeCalendarView = MutableLiveData<List<Calendar>?>()
    val homeErrorMessage = MutableLiveData<Boolean>()
    val homeNullMessage = MutableLiveData<Boolean>()
    val homeSearchView = MutableLiveData<Boolean>()
    val homeUserName = MutableLiveData<String?>()
    val homeToastMessage = MutableLiveData<String?>()
    val homeSnackbarMessage = MutableLiveData<Int>()
    val homeDateText = MutableLiveData<String?>()

    private val calendarService = CalendarService()
    private val disposable = CompositeDisposable()

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
        getCalendarImageFromAPI()
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


    private fun getCalendarImageFromAPI(){

        disposable.add(
            calendarService.getCalendarData()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Calendar>>(){
                    override fun onSuccess(calendarList: List<Calendar>) {

                        val month = datePicker.currentMonth()

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

                            val newList: MutableList<Calendar> = mutableListOf()
                            for (i in indexOfMonth until calendarList.size) {
                                newList.add(calendarList[i])
                            }
                            for (i in 0 until indexOfMonth) {
                                newList.add(calendarList[i])
                            }

                            val newPhotoList = newList.toTypedArray()
                            val newPhotoArrayList = ArrayList<Calendar>(newPhotoList.asList())

                            homeCalendarView.value = newPhotoArrayList

                        }


                    }

                    override fun onError(e: Throwable) {
                        homeToastMessage.value = e.localizedMessage
                    }

                })
        )

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

    override fun onCleared() {
        super.onCleared()

        disposable.clear()
    }

}