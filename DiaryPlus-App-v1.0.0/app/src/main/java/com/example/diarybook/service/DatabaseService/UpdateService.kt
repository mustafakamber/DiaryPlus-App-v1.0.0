package com.example.diarybook.service.DatabaseService

import android.net.Uri
import androidx.core.net.toUri
import com.example.diarybook.model.Diary
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class UpdateService {


    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()


    suspend fun updateNoteToFirebase(updatedDiary: Diary, deletePhotoList: MutableList<Uri>)
            : Result<Unit> {
        return suspendCoroutine { continuation ->

            if (updatedDiary.diaryPhoto!!.isNotEmpty()) {

                val uuid = UUID.randomUUID().toString()

                val addPhotoUrls = mutableListOf<String>()
                val firebasePhotoUrls = mutableListOf<String>()
                val updatedToFirebasePhotoUrls = mutableListOf<String>()

                val noteRef = database.collection("Notes")
                    .whereEqualTo("id", updatedDiary.diaryId)

                noteRef.get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val noteData = document.data
                            if (noteData != null) {
                                val photoUrls =
                                    noteData.get("photo") as? List<String>? ?: emptyList()
                                firebasePhotoUrls.addAll(photoUrls)
                                updatedToFirebasePhotoUrls.addAll(firebasePhotoUrls)
                                if (photoUrls.isNotEmpty()) {
                                    if (deletePhotoList.size != 0) {
                                        val deleteTasks = mutableListOf<Task<Void>>()

                                        for (deletePhotoUrl in deletePhotoList) {
                                            val photoReference =
                                                storage.getReferenceFromUrl(deletePhotoUrl.toString())
                                            val deleteTask = photoReference.delete()
                                            deleteTasks.add(deleteTask)
                                        }

                                        Tasks.whenAllSuccess<Void>(deleteTasks)
                                            .addOnSuccessListener {
                                                for (deletedPhoto in deletePhotoList) {
                                                    var photoDeleted = false

                                                    for (firebasePhoto in firebasePhotoUrls) {
                                                        if (deletedPhoto == firebasePhoto.toUri()) {
                                                            updatedToFirebasePhotoUrls.remove(deletedPhoto.toString())
                                                            firebasePhotoUrls.remove(deletedPhoto.toString())
                                                            photoDeleted = true
                                                            break
                                                        }
                                                    }
                                                }
                                                val uploadedPhotoReferences =
                                                    mutableListOf<StorageReference>()
                                                for (photoUri in updatedDiary.diaryPhoto) {
                                                    val photoFileName =
                                                        "$uuid-${UUID.randomUUID()}.jpg"
                                                    val photoReference =
                                                        storage.reference.child("noteImages")
                                                            .child(photoFileName)
                                                    val uploadTask =
                                                        photoReference.putFile(photoUri)
                                                    uploadTask.addOnSuccessListener { taskSnapshot ->
                                                        val uploadedPhotoReference =
                                                            taskSnapshot.storage
                                                        uploadedPhotoReferences.add(
                                                            uploadedPhotoReference
                                                        )
                                                        if (uploadedPhotoReferences.size == updatedDiary.diaryPhoto.size) {
                                                            uploadedPhotoReferences.forEachIndexed { index, reference ->
                                                                reference.downloadUrl.addOnSuccessListener { uri ->
                                                                    val downloadUrl = uri.toString()
                                                                    addPhotoUrls.add(downloadUrl)
                                                                    if (addPhotoUrls.size == updatedDiary.diaryPhoto.size) {
                                                                        updatedToFirebasePhotoUrls.addAll(
                                                                            addPhotoUrls
                                                                        )
                                                                        document.reference.update(
                                                                            "title",
                                                                            updatedDiary.diaryTitle,
                                                                            "content",
                                                                            updatedDiary.diaryContent,
                                                                            "photo",
                                                                            updatedToFirebasePhotoUrls,
                                                                            "date en",
                                                                            updatedDiary.diaryDateEn,
                                                                            "date tr",
                                                                            updatedDiary.diaryDateTr,
                                                                            "background color",
                                                                            updatedDiary.diaryBackgroundColor,
                                                                            "text color",
                                                                            updatedDiary.diaryTextColor,
                                                                            "time",
                                                                            updatedDiary.diaryTime,
                                                                            "photoFileName",
                                                                            updatedToFirebasePhotoUrls.map {
                                                                                val uri =
                                                                                    Uri.parse(it)
                                                                                uri.lastPathSegment
                                                                            }
                                                                        ).addOnSuccessListener {
                                                                            continuation.resume(
                                                                                Result.success(Unit)
                                                                            )
                                                                        }
                                                                            .addOnFailureListener { error ->
                                                                                continuation.resumeWithException(
                                                                                    error
                                                                                )
                                                                            }
                                                                    }
                                                                }.addOnFailureListener { error ->
                                                                    continuation.resumeWithException(
                                                                        error
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            }.addOnFailureListener { error ->
                                                continuation.resumeWithException(error)
                                            }

                                    } else {
                                        val uploadedPhotoReferences =
                                            mutableListOf<StorageReference>()
                                        for (photoUri in updatedDiary.diaryPhoto) {
                                            val photoFileName = "$uuid-${UUID.randomUUID()}.jpg"
                                            val photoReference =
                                                storage.reference.child("noteImages")
                                                    .child(photoFileName)
                                            val uploadTask = photoReference.putFile(photoUri)
                                            uploadTask.addOnSuccessListener { taskSnapshot ->
                                                val uploadedPhotoReference = taskSnapshot.storage
                                                uploadedPhotoReferences.add(uploadedPhotoReference)
                                                if (uploadedPhotoReferences.size == updatedDiary.diaryPhoto.size) {
                                                    uploadedPhotoReferences.forEachIndexed { index, reference ->
                                                        reference.downloadUrl.addOnSuccessListener { uri ->
                                                            val downloadUrl = uri.toString()
                                                            addPhotoUrls.add(downloadUrl)
                                                            if (addPhotoUrls.size == updatedDiary.diaryPhoto.size) {
                                                                updatedToFirebasePhotoUrls.addAll(addPhotoUrls)
                                                                document.reference.update(
                                                                    "title",
                                                                    updatedDiary.diaryTitle,
                                                                    "content",
                                                                    updatedDiary.diaryContent,
                                                                    "photo",
                                                                    updatedToFirebasePhotoUrls,
                                                                    "date en",
                                                                    updatedDiary.diaryDateEn,
                                                                    "date tr",
                                                                    updatedDiary.diaryDateTr,
                                                                    "background color",
                                                                    updatedDiary.diaryBackgroundColor,
                                                                    "text color",
                                                                    updatedDiary.diaryTextColor,
                                                                    "time",
                                                                    updatedDiary.diaryTime,
                                                                    "photoFileName",
                                                                    updatedToFirebasePhotoUrls.map {
                                                                        val uri = Uri.parse(it)
                                                                        uri.lastPathSegment
                                                                    }
                                                                ).addOnSuccessListener {
                                                                    continuation.resume(
                                                                        Result.success(
                                                                            Unit
                                                                        )
                                                                    )
                                                                }.addOnFailureListener { error ->
                                                                    continuation.resumeWithException(
                                                                        error
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val uploadedPhotoReferences = mutableListOf<StorageReference>()
                                    for (photoUri in updatedDiary.diaryPhoto) {
                                        val photoFileName = "$uuid-${UUID.randomUUID()}.jpg"
                                        val photoReference = storage.reference.child("noteImages")
                                            .child(photoFileName)
                                        val uploadTask = photoReference.putFile(photoUri)
                                        uploadTask.addOnSuccessListener { taskSnapshot ->
                                            val uploadedPhotoReference = taskSnapshot.storage
                                            uploadedPhotoReferences.add(uploadedPhotoReference)
                                            if (uploadedPhotoReferences.size == updatedDiary.diaryPhoto.size) {
                                                uploadedPhotoReferences.forEachIndexed { index, reference ->
                                                    reference.downloadUrl.addOnSuccessListener { uri ->
                                                        val downloadUrl = uri.toString()
                                                        addPhotoUrls.add(downloadUrl)
                                                        if (addPhotoUrls.size == updatedDiary.diaryPhoto.size) {
                                                            updatedToFirebasePhotoUrls.addAll(addPhotoUrls)
                                                            document.reference.update(
                                                                "title",
                                                                updatedDiary.diaryTitle,
                                                                "content",
                                                                updatedDiary.diaryContent,
                                                                "photo",
                                                                updatedToFirebasePhotoUrls,
                                                                "date en",
                                                                updatedDiary.diaryDateEn,
                                                                "date tr",
                                                                updatedDiary.diaryDateTr,
                                                                "background color",
                                                                updatedDiary.diaryBackgroundColor,
                                                                "text color",
                                                                updatedDiary.diaryTextColor,
                                                                "time",
                                                                updatedDiary.diaryTime,
                                                                "photoFileName",
                                                                updatedToFirebasePhotoUrls.map {
                                                                    val uri = Uri.parse(it)
                                                                    uri.lastPathSegment
                                                                }
                                                            ).addOnSuccessListener {
                                                                continuation.resume(
                                                                    Result.success(
                                                                        Unit
                                                                    )
                                                                )
                                                            }.addOnFailureListener { error ->
                                                                continuation.resumeWithException(
                                                                    error
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }

            } else {
                val oldPhotoUrls = mutableListOf<String>()
                val updatedPhotoUrls = mutableListOf<String>()
                val noteRef = database.collection("Notes")
                    .whereEqualTo("id", updatedDiary.diaryId)

                noteRef.get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val noteData = document.data
                            val photoUrls = noteData?.get("photo") as? List<String> ?: emptyList()
                            oldPhotoUrls.addAll(photoUrls)
                            updatedPhotoUrls.addAll(oldPhotoUrls)
                            if (photoUrls.isNotEmpty()) {
                                if (deletePhotoList.size != 0) {
                                    val deleteTasks = mutableListOf<Task<Void>>()

                                    for (deletePhotoUrl in deletePhotoList) {
                                        val photoReference =
                                            storage.getReferenceFromUrl(deletePhotoUrl.toString())
                                        val deleteTask = photoReference.delete()
                                        deleteTasks.add(deleteTask)
                                    }

                                    Tasks.whenAllSuccess<Void>(deleteTasks)
                                        .addOnSuccessListener {
                                            for (deletedPhoto in deletePhotoList) {
                                                for (oldPhotoUrl in oldPhotoUrls) {
                                                    if (deletedPhoto == oldPhotoUrl.toUri()) {
                                                        updatedPhotoUrls.remove(deletedPhoto.toString())
                                                    }
                                                }
                                            }
                                            document.reference.update(
                                                "title", updatedDiary.diaryTitle,
                                                "content", updatedDiary.diaryContent,
                                                "date en", updatedDiary.diaryDateEn,
                                                "date tr", updatedDiary.diaryDateTr,
                                                "background color", updatedDiary.diaryBackgroundColor,
                                                "text color", updatedDiary.diaryTextColor,
                                                "time", updatedDiary.diaryTime,
                                                "photoFileName", updatedPhotoUrls.map {
                                                    val uri = Uri.parse(it)
                                                    uri.lastPathSegment
                                                },
                                                "photo", updatedPhotoUrls
                                            )
                                                .addOnSuccessListener {
                                                    continuation.resume(Result.success(Unit))
                                                }
                                                .addOnFailureListener { error ->
                                                    continuation.resumeWithException(error)
                                                }
                                        }
                                        .addOnFailureListener { error ->
                                            continuation.resumeWithException(error)
                                        }
                                } else {
                                    document.reference.update(
                                        "title", updatedDiary.diaryTitle,
                                        "content", updatedDiary.diaryContent,
                                        "date en", updatedDiary.diaryDateEn,
                                        "date tr", updatedDiary.diaryDateTr,
                                        "background color", updatedDiary.diaryBackgroundColor,
                                        "text color", updatedDiary.diaryTextColor,
                                        "time", updatedDiary.diaryTime,
                                    )
                                        .addOnSuccessListener {
                                            continuation.resume(Result.success(Unit))
                                        }
                                        .addOnFailureListener { error ->
                                            continuation.resumeWithException(error)
                                        }
                                }
                            } else {
                                document.reference.update(
                                    "title", updatedDiary.diaryTitle,
                                    "content", updatedDiary.diaryContent,
                                    "date en", updatedDiary.diaryDateEn,
                                    "date tr", updatedDiary.diaryDateTr,
                                    "background color", updatedDiary.diaryBackgroundColor,
                                    "text color", updatedDiary.diaryTextColor,
                                    "time", updatedDiary.diaryTime,
                                )
                                    .addOnSuccessListener {
                                        continuation.resume(Result.success(Unit))
                                    }
                                    .addOnFailureListener { error ->
                                        continuation.resumeWithException(error)
                                    }
                            }

                        }
                    }
                    .addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }
            }
        }
    }
}