package com.example.diarybook.viewmodel

import android.app.Application
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

    val archiveDiaryView = MutableLiveData<List<Diary>?>()
    val archiveNotesErrorMessage = MutableLiveData<Boolean>()
    val archiveNoteNullMessage = MutableLiveData<Boolean>()
    val archiveSearchView = MutableLiveData<Boolean>()
    val archiveToastMessage = MutableLiveData<String?>()
    val archiveSnackbarMessage = MutableLiveData<Int>()

    private val getService = GetService()
    private val moveService = MoveService()
    private val deleteService = DeleteService()

    private val sharedPreferences = SharedPreferences(getApplication())


    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun refreshArchive(){
        archiveDiaryView.value = null
        archiveNoteNullMessage.value = false
        archiveNotesErrorMessage.value = false
        archiveSearchView.value = false
        archiveToastMessage.value = null
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
                        archiveNoteNullMessage.value = true
                    } else {
                        archiveDiaryView.value = archiveNotes
                    }
                }
            } catch (error: Exception) {
                archiveNotesErrorMessage.value = true
                withContext(Dispatchers.Main) {
                    archiveToastMessage.value = error.localizedMessage
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
                archiveSnackbarMessage.value = R.string.deleted
                getDiariesInTheArchive()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    archiveToastMessage.value = error.localizedMessage
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
                archiveSnackbarMessage.value = R.string.unarchived
                getDiariesInTheArchive()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    archiveToastMessage.value = error.localizedMessage
                }
            }
        }
    }

}