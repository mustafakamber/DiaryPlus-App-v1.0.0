package com.example.diarybook.service.DatabaseService

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DeleteService {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()

    var currentUser = auth.currentUser


    suspend fun deleteAllArchiveFromFirebase(): Result<Unit> {
        return suspendCoroutine { continuation ->

            val archiveRef = database.collection("Archives")
                .whereEqualTo("user", currentUser!!.email)
            archiveRef.get()
                .addOnSuccessListener { documents ->
                    val deleteTasks = mutableListOf<Task<Void>>()

                    for (document in documents) {
                        val archiveData = document.data
                        document.reference.delete()

                        val photoUrls = archiveData["photo"] as? List<String> ?: emptyList()

                        if (photoUrls.isNotEmpty()) {
                            for (photoUrl in photoUrls) {
                                val sourcePhotoReference = storage.getReferenceFromUrl(photoUrl)
                                val photoFileName = sourcePhotoReference.name
                                val destPhotoReference =
                                    storage.reference.child("archiveImages").child(photoFileName)
                                val deleteTask = destPhotoReference.delete()
                                deleteTasks.add(deleteTask)
                            }
                        }
                    }

                    Tasks.whenAllSuccess<Void>(deleteTasks)
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
        }
    }

    suspend fun deleteSingleArchiveFromFirebase(noteId: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            val archiveRef = database.collection("Archives")
                .whereEqualTo("id", noteId)
            archiveRef.get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {
                        val archiveData = document.data
                        document.reference.delete()
                            .addOnSuccessListener {
                                val photoUrls =
                                    archiveData.get("photo") as? List<String> ?: emptyList()
                                if (photoUrls.isNotEmpty()) {

                                    for (photoUrl in photoUrls) {
                                        val sourcePhotoReference =
                                            storage.getReferenceFromUrl(photoUrl)
                                        val photoFileName = sourcePhotoReference.name
                                        val destPhotoReference =
                                            storage.reference.child("archiveImages")
                                                .child(photoFileName)
                                        destPhotoReference.delete()
                                            .addOnFailureListener { error ->
                                                continuation.resumeWithException(error)
                                            }
                                    }
                                }
                                continuation.resume(Result.success(Unit))
                            }
                            .addOnFailureListener { error ->
                                continuation.resumeWithException(error)
                            }
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }

    suspend fun deleteSingleNoteFromFirebase(noteId: String): Result<Unit> {
        return suspendCoroutine { continuation ->

            val noteRef = database.collection("Notes")
                .whereEqualTo("id", noteId)

            noteRef.get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {
                        val noteData = document.data
                        document.reference.delete()
                            .addOnSuccessListener {
                                val photoUrls =
                                    noteData.get("photo") as? List<String> ?: emptyList()
                                if (photoUrls.isNotEmpty()) {
                                    for (photoUrl in photoUrls) {
                                        val photoReference = storage.getReferenceFromUrl(photoUrl)
                                        photoReference.delete()
                                            .addOnFailureListener { error ->
                                                continuation.resumeWithException(error)
                                            }
                                    }
                                }
                                continuation.resume(Result.success(Unit))
                            }
                            .addOnFailureListener { error ->
                                continuation.resumeWithException(error)
                            }
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }


    suspend fun deleteAllNoteFromFirebase(): Result<Unit> {
        return suspendCoroutine { continuation ->

            val noteRef = database.collection("Notes")
                .whereEqualTo("user", currentUser!!.email)

            noteRef.get()
                .addOnSuccessListener { documents ->
                    val deleteTasks = mutableListOf<Task<Void>>()

                    for (document in documents) {
                        val noteData = document.data
                        document.reference.delete()

                        val photoUrls = noteData["photo"] as? List<String> ?: emptyList()

                        if (photoUrls.isNotEmpty()) {
                            for (photoUrl in photoUrls) {
                                val photoReference = storage.getReferenceFromUrl(photoUrl)
                                val deleteTask = photoReference.delete()
                                deleteTasks.add(deleteTask)
                            }
                        }
                    }

                    Tasks.whenAllSuccess<Void>(deleteTasks)
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
        }
    }

}