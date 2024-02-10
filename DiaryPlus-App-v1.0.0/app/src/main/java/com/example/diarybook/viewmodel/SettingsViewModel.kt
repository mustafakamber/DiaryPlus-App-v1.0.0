package com.example.diarybook.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.model.User
import com.example.diarybook.service.*
import com.example.diarybook.service.DatabaseService.CloudService
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.MoveService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel (application: Application) : UserViewModel(application) {


    private val moveService = MoveService()
    private val cloudService = CloudService()
    private val deleteService = DeleteService()
    private val authService = AuthService()
    private val getService = GetService()

    private val sharedPreferences = SharedPreferences(getApplication())

    val userData = MutableLiveData<User>()
    val settingsSnackbarMessage = MutableLiveData<Int>()
    val settingsToastMessage = MutableLiveData<String>()

    fun saveStringData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }


    fun getUserInfoFromDB() {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.getUserInfo()
                }
                val result = authService.getUserInfo()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        userData.value = user!!
                    }
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }


    fun backupAllNotes() {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    cloudService.backupAllNotesFromFirebase()
                }
                settingsSnackbarMessage.value = R.string.all_backup
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun backloadAllNotes(){
        launch {
            try {
                withContext(Dispatchers.IO){
                    cloudService.backloadAllDiariesFromFirebase()
                }
                settingsSnackbarMessage.value = R.string.all_backload
            }
            catch (error : Exception){
                withContext(Dispatchers.Main){
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }


    fun deleteCloud(){
        launch {
            try {
                withContext(Dispatchers.IO){
                    cloudService.deleteAllBackupDataFromFirebase()
                }
                settingsSnackbarMessage.value = R.string.all_cleared
            }
            catch (error : Exception){
                withContext(Dispatchers.Main){
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }


    fun deleteAllNotes() {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    deleteService.deleteAllNoteFromFirebase()
                }
                settingsSnackbarMessage.value = R.string.all_deleted
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.currentUserDeleteAccount()
                }
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun moveAllNoteToArchive() {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    moveService.moveAllNoteToArchiveFirebase()
                }
                settingsSnackbarMessage.value = R.string.all_archived

            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun moveAllArchiveToNote() {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    moveService.moveAllArchiveToNoteFirebase()
                }
                settingsSnackbarMessage.value = R.string.all_noted
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun deleteAllArchive(onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    deleteService.deleteAllArchiveFromFirebase()
                }
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun getPhotos(
        onPhotosReady: (MutableList<Uri>) -> Unit,
        onPhotoNull: () -> Unit
    ) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    getService.getAllPhotosFromFirebase()
                }
                val result = getService.getAllPhotosFromFirebase()
                if (result.isSuccess) {
                    val photos = result.getOrThrow()
                    if (photos.size == 0) {
                        onPhotoNull()
                    } else {
                        withContext(Dispatchers.Main) {
                            onPhotosReady.invoke(photos)
                        }
                    }
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun logOut(onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.currentUserLogOut()
                }
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    settingsToastMessage.value = error.localizedMessage
                }
            }
        }
    }

}