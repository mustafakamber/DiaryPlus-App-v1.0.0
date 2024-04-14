package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.model.Diary
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.MoveService
import com.example.diarybook.util.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArchiveViewModel (application: Application) : CoroutineViewModel(application) {

    private val _archiveDiaryView = MutableLiveData<List<Diary>?>()
    val archiveDiaryView : LiveData<List<Diary>?>
        get() = _archiveDiaryView

    private val _archiveErrorMessage = MutableLiveData<Boolean>()
    val archiveErrorMessage : LiveData<Boolean>
        get() = _archiveErrorMessage

    private val _archiveNullMessage = MutableLiveData<Boolean>()
    val archiveNullMessage : LiveData<Boolean>
        get() = _archiveNullMessage

    private val _archiveSearchView = MutableLiveData<Boolean>()
    val archiveSearchView : LiveData<Boolean>
        get() = _archiveSearchView

    private val _archiveToastMessage = MutableLiveData<String?>()
    val archiveToastMessage : LiveData<String?>
        get() = _archiveToastMessage

    private val _archiveSnackbarMessage = MutableLiveData<Int>()
    val archiveSnackbarMessage : LiveData<Int>
        get() = _archiveSnackbarMessage

    private val getService = GetService()
    private val moveService = MoveService()
    private val deleteService = DeleteService()
    private val sharedPreferences = SharedPreferences(getApplication())
    private var isSearchViewVisible: Boolean = false


    fun updateSearchViewVisibilityState(){
        _archiveSearchView.value = !isSearchViewVisible
        isSearchViewVisible = !isSearchViewVisible
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun refreshArchive(){
        _archiveDiaryView.value = null
        _archiveNullMessage.value = false
        _archiveErrorMessage.value = false
        _archiveSearchView.value = false
        _archiveToastMessage.value = null
        getDiariesInTheArchive()
    }

    fun getDiariesInTheArchive() {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    getService.getAllArchiveNotesFromFirebase()
                }
                val result = getService.getAllArchiveNotesFromFirebase()
                if (result.isSuccess) {
                    val archiveNotes = result.getOrNull()
                    if (archiveNotes.isNullOrEmpty()) {
                        _archiveNullMessage.value = true
                    } else {
                        _archiveDiaryView.value = archiveNotes
                    }
                }
            } catch (error: Exception) {
                _archiveErrorMessage.value = true
                withContext(Dispatchers.Main) {
                    _archiveToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun deleteArchive(noteId: String) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    deleteService.deleteSingleArchiveFromFirebase(noteId)
                }
                _archiveSnackbarMessage.value = R.string.deleted
                getDiariesInTheArchive()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _archiveToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun moveArchiveToNote(noteId: String) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    moveService.moveSingleArchiveToNoteFirebase(noteId)
                }
                _archiveSnackbarMessage.value = R.string.unarchived
                getDiariesInTheArchive()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _archiveToastMessage.value = error.localizedMessage
                }
            }
        }
    }
}