package com.example.diarybook.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.CAMERA_PHOTO
import com.example.diarybook.constant.Constant.NOTE_DATA
import com.example.diarybook.constant.Constant.NULL_STRING
import com.example.diarybook.constant.Constant.OLD_DIARY_ID
import com.example.diarybook.model.Diary
import com.example.diarybook.service.DatabaseService.DeleteService
import com.example.diarybook.service.DatabaseService.GetService
import com.example.diarybook.service.DatabaseService.SaveService
import com.example.diarybook.service.DatabaseService.UpdateService
import com.example.diarybook.util.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryViewModel (application: Application) : CoroutineViewModel(application) {

    private val saveService = SaveService()
    private val getService = GetService()
    private val deleteService = DeleteService()
    private val updateService = UpdateService()

    private val sharedPreferences = SharedPreferences(getApplication())

    private val _photoViewVisible = MutableLiveData<Boolean>()
    val photoViewVisible : LiveData<Boolean>
        get() = _photoViewVisible

    private val _photoFromGalleryCamera = MutableLiveData<Uri>()
    val photoFromGalleryCamera : LiveData<Uri>
        get() = _photoFromGalleryCamera

    private val _exitSuccess = MutableLiveData<Boolean>()
    val exitSuccess: LiveData<Boolean>
        get() = _exitSuccess

    private val _gallerySuccess = MutableLiveData<Boolean>()
    val gallerySuccess: LiveData<Boolean>
        get() = _gallerySuccess

    private val _titleErrorText = MutableLiveData<Int>()
    val titleErrorText: LiveData<Int>
        get() = _titleErrorText

    private val _titleEndIconVisible = MutableLiveData<Boolean>()
    val titleEndIconVisible: LiveData<Boolean>
        get() = _titleEndIconVisible

    private val _contentErrorText = MutableLiveData<Int>()
    val contentErrorText: LiveData<Int>
        get() = _contentErrorText

    private val _contentEndIconVisible = MutableLiveData<Boolean>()
    val contentEndIconVisible: LiveData<Boolean>
        get() = _contentEndIconVisible

    val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val _permissionMessage = MutableLiveData<String>()
    val permissionMessage: LiveData<String>
        get() = _permissionMessage

    private val _snackbarMessage = MutableLiveData<Int>()
    val snackbarMessage: LiveData<Int>
        get() = _snackbarMessage

    data class ActionSnackbarMessage(
        val snackbarText: Int,
        val allowButtonText: Int,
        val permissionMessage: String
    )

    private val _actionSnackbarMessage = MutableLiveData<ActionSnackbarMessage>()
    val actionSnackbarMessage: LiveData<ActionSnackbarMessage>
        get() = _actionSnackbarMessage

    data class OldNewCheck(
        val isNewDiary : Boolean,
        val diary : Diary?
    )

    private val _newDiaryCheck = MutableLiveData<OldNewCheck>()
    val newDiaryCheck : LiveData<OldNewCheck>
        get() = _newDiaryCheck

    init {
        newDiaryCheck()
    }

    private fun newDiaryCheck() {
        val isNewNote = getBooleanData(NOTE_DATA)
        if (isNewNote) {
            updateNewDiary(true,null)
        } else {
            val noteId = getStringData(OLD_DIARY_ID).toString()
            launch {
                try {
                    withContext(Dispatchers.IO) {
                        getService.getSingleNoteFromFirebase(noteId)
                    }
                    val result = getService.getSingleNoteFromFirebase(noteId)
                    if (result.isSuccess) {
                        val diary = result.getOrNull()
                        diary?.let {
                            updateNewDiary(false,it)
                        }
                    }
                } catch (error: Exception) {
                    withContext(Dispatchers.Main) {
                        updateNewDiary(true,null)
                        _toastMessage.value = error.localizedMessage
                    }
                }
            }
        }
    }

    fun updateNewDiary(isNew : Boolean, diaryData: Diary?) {
        val isNewDiary = OldNewCheck(isNew,diaryData)
        _newDiaryCheck.value = isNewDiary
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

    fun setVisibleTitleEndIcon() {
        _titleEndIconVisible.value = true
    }

    fun setVisibleContentEndIcon() {
        _contentEndIconVisible.value = true
    }

    fun inputControl(
        id: String?,
        title: String,
        content: String,
        newPhoto: MutableList<Uri>,
        deletedPhoto: MutableList<Uri>?,
        textColor: String,
        backgroundColor: String,
        dateTr: String,
        dateEn: String,
        time: String,
    ) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            if (TextUtils.isEmpty(title)) {
                _titleEndIconVisible.value = false
                _titleErrorText.value = R.string.note_title_error_message
            }
            if (TextUtils.isEmpty(content)) {
                _contentEndIconVisible.value = false
                _contentErrorText.value = R.string.note_content_error_message
            }
            //noteSaveButton.isClickable = true
            //noteMenuButton.isClickable = true
            return
        } else {
            if (id == null) {
                saveDiary(
                    title,
                    content,
                    newPhoto,
                    dateEn,
                    dateTr,
                    backgroundColor,
                    textColor,
                    time
                )
            } else {
                updateDiary(
                    deletedPhoto!!,
                    id,
                    title,
                    content,
                    newPhoto,
                    dateEn,
                    dateTr,
                    backgroundColor,
                    textColor,
                    time
                )
            }
        }
    }

    private fun saveDiary(
        title: String, content: String,
        photo: MutableList<Uri>?, dateEn: String, dateTr: String,
        backgroundColor: String, textColor: String, time: String
    ) {
        val newDiary = Diary(
            null,
            title, content,
            photo, dateEn, dateTr,
            backgroundColor, textColor, time
        )
        launch {
            try {
                withContext(Dispatchers.IO) {
                    saveService.saveSingleNoteToFirebase(newDiary)
                }
                _snackbarMessage.value = R.string.saved
                successfullyExit()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
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
                _snackbarMessage.value = R.string.deleted
                successfullyExit()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
                }
            }
        }
    }



    private fun successfullyExit() {
        Handler().postDelayed({
            _exitSuccess.value = true
        }, 750)
    }

    fun handleGalleryPermissionResult(context : Context,result : Boolean){
        if (result) {
            _gallerySuccess.value = true
        } else {
            _toastMessage.value = context.getString(R.string.read_external_storage_permission)
        }
    }

    private fun updateDiary(
        deletedPhotoList: MutableList<Uri>, id: String, title: String,
        content: String, photo: MutableList<Uri>?, dateEn: String,
        dateTr: String, backgroundColor: String, textColor: String,
        time: String
    ) {
        val updatedDiary = Diary(
            id, title, content,
            photo, dateEn, dateTr,
            backgroundColor, textColor, time
        )
        launch {
            try {
                withContext(Dispatchers.IO) {
                    updateService.updateNoteToFirebase(updatedDiary, deletedPhotoList)
                }
                _snackbarMessage.value = R.string.updated
                successfullyExit()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun checkSelfPermissionAccessToGallery(
        activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED)
        ) {
            _gallerySuccess.value = true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                updateActionSnackbarMessage(
                    R.string.read_external_storage_permission,
                    R.string.allow,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                _permissionMessage.value = Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }
    }

    fun updateActionSnackbarMessage(firstValue: Int, secondValue: Int, message: String) {
        val snackbarMessage = ActionSnackbarMessage(firstValue, secondValue, message)
        _actionSnackbarMessage.value = snackbarMessage
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

    fun getPhotoFromGallery(galleryResult: ActivityResult) {
        if (galleryResult.resultCode == AppCompatActivity.RESULT_OK) {
            val intentFromGalleryResult = galleryResult.data
            if (intentFromGalleryResult != null) {
                _photoFromGalleryCamera.value = intentFromGalleryResult.data
            }
        }
    }
    fun getPhotoFromCamera() {
        val selectedCameraPhotoString = getStringData(CAMERA_PHOTO)
        if (selectedCameraPhotoString != NULL_STRING) {
            _photoFromGalleryCamera.value = selectedCameraPhotoString!!.toUri()
        }
    }

}