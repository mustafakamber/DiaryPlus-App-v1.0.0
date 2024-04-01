package com.example.diarybook.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.model.Diary
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.SaveService
import com.example.diarybook.service.DatabaseService.UpdateService
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.constant.Constant.NOTE_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryViewModel (application: Application) : CoroutineViewModel(application) {


    private val saveService = SaveService()
    private val getService = GetService()
    private val deleteService = DeleteService()
    private val updateService = UpdateService()

    private val sharedPreferences = SharedPreferences(getApplication())


    var selectedNotePhoto: Uri? = null


    val showOldDiary = MutableLiveData<Diary>()
    val createNewDiary = MutableLiveData<Boolean>()
    val diaryToastMessage = MutableLiveData<String>()
    val diarySnackbarMessage = MutableLiveData<Int>()

    fun newDiaryCheck() {
        createNewDiary.value = false

        val isNewNote = getBooleanData(NOTE_DATA)

        if (isNewNote) {
            createNewDiary.value = true
        } else {
            val noteId = getStringData("noteId").toString()

            launch {
                try {
                    withContext(Dispatchers.IO) {
                        getService.getSingleNoteFromFirebase(noteId)
                    }
                    val result = getService.getSingleNoteFromFirebase(noteId)
                    if (result.isSuccess) {
                        val note = result.getOrNull()
                        note?.let {
                            showOldDiary.value = note!!
                        }
                    }
                } catch (error: Exception) {
                    withContext(Dispatchers.Main) {
                        diaryToastMessage.value = error.localizedMessage
                    }
                }
            }
        }
    }


    fun saveStringData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun getStringData(key: String): String? {
        return sharedPreferences.getStringData(key)
    }

    fun getBooleanData(key: String): Boolean {
        return sharedPreferences.getBooleanData(key)
    }

    fun deleteData(key: String) {
        sharedPreferences.deleteData(key)
    }

    fun saveDiary(
        diaryId: String?, diaryTitle: String?, diaryContent: String?,
        diaryPhoto: MutableList<Uri>?, diaryDateEn: String?, diaryDateTr: String?,
        noteBackgroundColor: String?, noteTextColor: String?, noteTime: String?,
        onSuccess: () -> Unit
    ) {

        val newDiary = Diary(
            diaryId, diaryTitle, diaryContent,
            diaryPhoto, diaryDateEn, diaryDateTr,
            noteBackgroundColor, noteTextColor, noteTime
        )

        launch {
            try {
                withContext(Dispatchers.IO) {
                    saveService.saveSingleNoteToFirebase(newDiary)
                }
                diarySnackbarMessage.value = R.string.saved
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    diaryToastMessage.value = error.localizedMessage
                }
            }
        }

    }

    fun deleteNote(noteId: String, onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    deleteService.deleteSingleNoteFromFirebase(noteId)
                }
                diarySnackbarMessage.value = R.string.deleted
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    diaryToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun updateDiary(
        deletedPhotoList: MutableList<Uri>, diaryId: String?, diaryTitle: String?,
        diaryContent: String?, diaryPhoto: MutableList<Uri>?, diaryDateEn: String?,
        diaryDateTr: String?, noteBackgroundColor: String?, noteTextColor: String?,
        noteTime: String?, onSuccess: () -> Unit
    ) {

        val updatedDiary = Diary(
            diaryId, diaryTitle, diaryContent,
            diaryPhoto, diaryDateEn, diaryDateTr,
            noteBackgroundColor, noteTextColor, noteTime
        )


        launch {
            try {
                withContext(Dispatchers.IO) {
                    updateService.updateNoteToFirebase(updatedDiary, deletedPhotoList)
                }
                diarySnackbarMessage.value = R.string.updated
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    diaryToastMessage.value = error.localizedMessage
                }
            }
        }
    }


    fun checkSelfPermissionAccessToGallery(
        activity: Activity,
        onSuccess: () -> Unit,
        withoutSnackbarREAD: () -> Unit,
        getSnackbarREAD: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED)
        ) {
            onSuccess()
        } else {
            // Izin verilmemişse
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                getSnackbarREAD()
            } else {
                withoutSnackbarREAD()
            }
        }
    }

    fun checkSelfPermissionAccessToCamera(activity: Activity) {
        val permissionList = mutableListOf<String>()

        if (checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionList.size > 0) {
            activity.requestPermissions(permissionList.toTypedArray(), 61)
        }
    }

    fun getPhotoFromGallery(galleryResult: ActivityResult, onPhotoReady: (Uri?) -> Unit) {

        if (galleryResult.resultCode == AppCompatActivity.RESULT_OK) {
            val intentFromGalleryResult = galleryResult.data
            if (intentFromGalleryResult != null) {
                selectedNotePhoto = intentFromGalleryResult.data
                onPhotoReady.invoke(selectedNotePhoto)
            }
        }
    }


}