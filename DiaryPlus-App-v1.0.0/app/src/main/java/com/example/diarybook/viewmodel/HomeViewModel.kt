package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.NULL_STRING
import com.example.diarybook.model.Calendar
import com.example.diarybook.model.Diary
import com.example.diarybook.service.AuthService
import com.example.diarybook.service.CalendarService
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.MoveService
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.view.dialog.DatePickerDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel (application: Application) : CoroutineViewModel(application) {

    private val _homeDiaryView = MutableLiveData<List<Diary>?>()
    val homeDiaryView : LiveData<List<Diary>?>
        get() = _homeDiaryView

    private val _homeCalendarView = MutableLiveData<List<Calendar>?>()
    val homeCalendarView : LiveData<List<Calendar>?>
        get() = _homeCalendarView

    private val _homeErrorMessage = MutableLiveData<Boolean>()
    val homeErrorMessage : LiveData<Boolean>
        get() = _homeErrorMessage

    private val _homeNullMessage = MutableLiveData<Boolean>()
    val homeNullMessage : LiveData<Boolean>
        get() = _homeNullMessage

    private val _homeSearchView = MutableLiveData<Boolean>()
    val homeSearchView : LiveData<Boolean>
        get() = _homeSearchView

    private val _homeUserName = MutableLiveData<String?>()
    val homeUserName : LiveData<String?>
        get() = _homeUserName

    private val _homeToastMessage = MutableLiveData<String?>()
    val homeToastMessage : LiveData<String?>
        get() = _homeToastMessage

    private val _homeSnackbarMessage = MutableLiveData<Int>()
    val homeSnackbarMessage : LiveData<Int>
        get() = _homeSnackbarMessage

    private val _homeDateText = MutableLiveData<String?>()
    val homeDateText : LiveData<String?>
        get() = _homeDateText

    private val calendarService = CalendarService()
    private val disposable = CompositeDisposable()

    private val getService = GetService()
    private val authService = AuthService()
    private val moveService = MoveService()
    private val deleteService = DeleteService()

    private val sharedPreferences = SharedPreferences(getApplication())
    private var isSearchViewVisible : Boolean = false
    val datePicker = DatePickerDialog(getApplication())

    fun updateSearchViewVisibilityState(){
        _homeSearchView.value = !isSearchViewVisible
        isSearchViewVisible = !isSearchViewVisible
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun refreshHomeFragment() {
        _homeUserName.value = null
        _homeDateText.value = null
        _homeDiaryView.value = null
        _homeCalendarView.value = null
        _homeToastMessage.value = null
        _homeErrorMessage.value = false
        _homeNullMessage.value = false
        _homeSearchView.value = false
        getUserName()
        getDate()
        getCalendarImageFromAPI()
        getAllDiaries()
    }

     private fun getAllDiaries(){
        launch {
            try {
                withContext(Dispatchers.IO) {
                    getService.getAllNotesFromFirebase()
                }
                val result = getService.getAllNotesFromFirebase()
                if (result.isSuccess) {
                    val notes = result.getOrNull()
                    if (notes.isNullOrEmpty()) {
                        _homeNullMessage.value = true
                    } else {
                        _homeDiaryView.value = notes
                    }
                }
            } catch (error: Exception) {
                _homeErrorMessage.value = true
                withContext(Dispatchers.Main) {
                    _homeToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private fun getDate(){
        val date = datePicker.currentDate()
        _homeDateText.value = date
    }

    private fun sortCalendarImage(photoList : List<Calendar>){
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
            for (i in indexOfMonth until photoList.size) {
                newList.add(photoList[i])
            }
            for (i in 0 until indexOfMonth) {
                newList.add(photoList[i])
            }
            val newPhotoList = newList.toTypedArray()
            val newPhotoArrayList = ArrayList<Calendar>(newPhotoList.asList())
            _homeCalendarView.value = newPhotoArrayList
        }
    }

    private fun getCalendarImageFromAPI(){
        disposable.add(
            calendarService.getCalendarData()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Calendar>>(){
                    override fun onSuccess(photoList: List<Calendar>) {
                        sortCalendarImage(photoList)
                    }
                    override fun onError(e: Throwable) {
                        _homeToastMessage.value = e.localizedMessage
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
                        _homeUserName.value = userName.userName!!.substringBefore(NULL_STRING)
                    }
                }
            }catch (error : Exception){
                withContext(Dispatchers.Main){
                    _homeToastMessage.value = error.localizedMessage
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
                _homeSnackbarMessage.value = R.string.deleted
                getAllDiaries()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _homeToastMessage.value = error.localizedMessage
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
                _homeSnackbarMessage.value = R.string.archived
                getAllDiaries()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _homeToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}