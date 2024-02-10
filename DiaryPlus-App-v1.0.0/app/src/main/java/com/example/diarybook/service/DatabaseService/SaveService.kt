package com.example.diarybook.service.DatabaseService

import android.net.Uri
import com.example.diarybook.model.Diary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SaveService {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()

    var currentUser = auth.currentUser

    suspend fun saveSingleNoteToFirebase(diary: Diary): Result<Unit> {
        return suspendCoroutine { continuation ->

            if (diary.diaryPhoto!!.isNotEmpty()) {
                val uuid = UUID.randomUUID().toString()
                val photoUrls = mutableListOf<String>()

                for (photoUri in diary.diaryPhoto) {
                    val photoFileName = "$uuid-${UUID.randomUUID()}.jpg"
                    val photoReference = storage.reference.child("noteImages").child(photoFileName)

                    val uploadTask = photoReference.putFile(photoUri)

                    uploadTask.addOnSuccessListener { taskSnapshot ->
                        val uploadedPhotoReference = taskSnapshot.storage
                        uploadedPhotoReference.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            photoUrls.add(downloadUrl)

                            if (photoUrls.size == diary.diaryPhoto.size) {
                                val noteMap = hashMapOf<String, Any>()

                                noteMap["id"] = diary.diaryId!!
                                noteMap["user"] = currentUser!!.email.toString()
                                noteMap["title"] = diary.diaryTitle!!
                                noteMap["content"] = diary.diaryContent!!
                                noteMap["background color"] = diary.diaryBackgroundColor!!
                                noteMap["text color"] = diary.diaryTextColor!!
                                noteMap["date en"] = diary.diaryDateEn!!
                                noteMap["date tr"] = diary.diaryDateTr!!
                                noteMap["photo"] = photoUrls
                                noteMap["time"] = diary.diaryTime!!
                                noteMap["photoFileName"] = photoUrls.map {
                                    val uri = Uri.parse(it)
                                    uri.lastPathSegment
                                }

                                database.collection("Notes").add(noteMap)
                                    .addOnSuccessListener {
                                        continuation.resume(Result.success(Unit))
                                    }
                                    .addOnFailureListener { error ->
                                        continuation.resumeWithException(error)
                                    }
                            }
                        }
                    }
                }
            } else {
                val noteMap = hashMapOf<String, Any>()

                noteMap["id"] = diary.diaryId!!
                noteMap["user"] = currentUser!!.email.toString()
                noteMap["title"] = diary.diaryTitle!!
                noteMap["content"] = diary.diaryContent!!
                noteMap["background color"] = diary.diaryBackgroundColor!!
                noteMap["text color"] = diary.diaryTextColor!!
                noteMap["date en"] = diary.diaryDateEn!!
                noteMap["date tr"] = diary.diaryDateTr!!
                noteMap["time"] = diary.diaryTime!!


                database.collection("Notes").add(noteMap)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }

            }
        }
    }
}