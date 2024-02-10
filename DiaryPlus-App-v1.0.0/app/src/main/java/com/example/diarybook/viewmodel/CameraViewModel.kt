package com.example.diarybook.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.util.Constant.FILE_PROVIDER_PACKAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraViewModel(application: Application) : CoroutineViewModel(application) {

    private val sharedPreferences = SharedPreferences(getApplication())
    val cameraToastMessage = MutableLiveData<String>()
    private val fileName = UUID.randomUUID().toString()

    fun savePhotoData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    fun saveImageToFile(
        activity: Activity, image: Image,
        onImageUriReady: (Uri) -> Unit
    ) {
        launch {

            val imageFile = File(activity.getExternalFilesDir(null), fileName)
            try {
                withContext(Dispatchers.IO) {
                    saveImageToFileCoroutine(imageFile, image)
                }
                image.close()
                val imageUri = FileProvider.getUriForFile(
                    activity,
                    FILE_PROVIDER_PACKAGE,
                    imageFile
                )
                rotateImageUri(imageUri, 90, activity) { rotatedImageUrlForFile ->
                    onImageUriReady.invoke(rotatedImageUrlForFile)
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    cameraToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private suspend fun saveImageToFileCoroutine(imageFile: File, image: Image): Result<Unit> {
        return suspendCoroutine { continuation ->
            try {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                FileOutputStream(imageFile).use { output ->
                    output.write(bytes)
                }

                continuation.resume(Result.success(Unit))
            } catch (error: Exception) {
                continuation.resumeWithException(error)
            }
        }
    }

    private fun rotateImageUri(
        uri: Uri, degrees: Int,
        context: Context,
        rotatedImageReady: (Uri) -> Unit
    ) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    val rotatedImageFile = rotatedImageFile(uri, degrees, context)
                    val rotatedImageUrlForFile = FileProvider.getUriForFile(
                        context,
                        FILE_PROVIDER_PACKAGE,
                        rotatedImageFile
                    )
                    rotatedImageReady.invoke(rotatedImageUrlForFile)
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    cameraToastMessage.value = error.localizedMessage
                }
            }
        }


    }

    private suspend fun rotatedImageFile(uri: Uri, degrees: Int, context: Context): File {
        return suspendCoroutine { continuation ->
            try {
                val input = context.contentResolver.openInputStream(uri)
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeStream(input, null, options)
                input?.close()

                val matrix = Matrix()
                matrix.postRotate(degrees.toFloat())

                val rotatedBitmap = bitmap?.let {
                    Bitmap.createBitmap(it, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }

                val output = ByteArrayOutputStream()
                rotatedBitmap?.let {
                    it.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }

                val rotatedImageByteArray = output.toByteArray()
                output.close()

                val rotatedImageFile = File(context.getExternalFilesDir(null),fileName)
                val fileOutputStream = FileOutputStream(rotatedImageFile)

                fileOutputStream.write(rotatedImageByteArray)
                fileOutputStream.close()

                continuation.resume(rotatedImageFile)
            } catch (error: Exception) {
                continuation.resumeWithException(error)
            }
        }
    }
}
