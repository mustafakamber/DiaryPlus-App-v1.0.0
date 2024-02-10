package com.example.diarybook.service.DatabaseService

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MoveService {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()

    var currentUser = auth.currentUser
    
    suspend fun moveAllArchiveToNoteFirebase(): Result<Unit> {
        return suspendCoroutine { continuation ->
            val archiveRef = database.collection("Archives")
                .whereEqualTo("user", currentUser!!.email)

            archiveRef.get()
                .addOnSuccessListener { documents ->
                    val noteCollectionRef = database.collection("Notes")
                    val moveTasks = mutableListOf<Task<Void>>()

                    for (document in documents) {
                        val archiveData = document.data

                        val noteDocRef =
                            noteCollectionRef.document(document.id)

                        noteDocRef.set(archiveData)
                            .addOnSuccessListener { noteDocRef ->
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        val photoUrls =
                                            archiveData.get("photo") as? List<String> ?: emptyList()
                                        if (photoUrls.isNotEmpty()) {
                                            for (photoUrl in photoUrls) {
                                                val oldSourcePhotoReference =
                                                    storage.getReferenceFromUrl(photoUrl)
                                                val oldPhotoArchiveFileName =
                                                    oldSourcePhotoReference.name
                                                val oldDestArchivePhotoReference =
                                                    storage.reference.child("archiveImages")
                                                        .child(oldPhotoArchiveFileName)
                                                val sourcePhotoReference =
                                                    oldDestArchivePhotoReference
                                                val photoFileName = sourcePhotoReference.name
                                                val destPhotoReference =
                                                    storage.reference.child("noteImages")
                                                        .child(photoFileName)
                                                val moveTask =
                                                    sourcePhotoReference.getBytes(Long.MAX_VALUE)
                                                        .addOnSuccessListener { sourceBytes ->
                                                            destPhotoReference.putBytes(sourceBytes)
                                                                .addOnSuccessListener {
                                                                    sourcePhotoReference.delete()
                                                                }
                                                        }
                                            }
                                        }
                                    }
                            }
                    }

                    Tasks.whenAllSuccess<Void>(moveTasks)
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


    suspend fun moveAllNoteToArchiveFirebase(): Result<Unit> {
        return suspendCoroutine { continuation ->
            val noteRef = database.collection("Notes")
                .whereEqualTo("user", currentUser!!.email)

            noteRef.get()
                .addOnSuccessListener { documents ->
                    val archiveCollectionRef = database.collection("Archives")
                    val moveTasks = mutableListOf<Task<Void>>()

                    for (document in documents) {

                        val archiveData = document.data

                        val archiveDocRef =
                            archiveCollectionRef.document(document.id)

                        archiveDocRef.set(archiveData)
                            .addOnSuccessListener { archiveDocRef ->
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


                                                val moveTask =
                                                    sourcePhotoReference.getBytes(Long.MAX_VALUE)
                                                        .addOnSuccessListener { sourceBytes ->
                                                            destPhotoReference.putBytes(sourceBytes)
                                                                .addOnSuccessListener {
                                                                    sourcePhotoReference.delete()
                                                                }
                                                        }
                                            }
                                        }
                                    }
                            }
                    }

                    Tasks.whenAllSuccess<Void>(moveTasks)
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


    suspend fun moveSingleArchiveToNoteFirebase(noteId: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            val archiveRef = database.collection("Archives")
                .whereEqualTo("id", noteId)
            archiveRef.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val noteCollectionRef = database.collection("Notes")


                        val archiveData = document.data

                        val noteDocRef =
                            noteCollectionRef.document(document.id)


                        noteDocRef.set(archiveData)
                            .addOnSuccessListener {
                                document.reference.delete()
                                val photoUrls = archiveData["photo"] as? List<String> ?: emptyList()

                                if (photoUrls.isNotEmpty()) {
                                    val copyTasks = mutableListOf<Task<Void>>()

                                    for (photoUrl in photoUrls) {
                                        val oldSourcePhotoReference =
                                            storage.getReferenceFromUrl(photoUrl)
                                        val oldPhotoArchiveFileName = oldSourcePhotoReference.name
                                        val oldDestArchivePhotoReference =
                                            storage.reference.child("archiveImages")
                                                .child(oldPhotoArchiveFileName)
                                        val sourcePhotoReference = oldDestArchivePhotoReference
                                        val photoFileName = sourcePhotoReference.name
                                        val destPhotoReference =
                                            storage.reference.child("noteImages")
                                                .child(photoFileName)

                                        val copyTask = sourcePhotoReference.getBytes(Long.MAX_VALUE)
                                            .addOnSuccessListener { sourceBytes ->
                                                destPhotoReference.putBytes(sourceBytes)
                                                    .addOnSuccessListener {
                                                        sourcePhotoReference.delete()
                                                    }
                                            }
                                    }
                                    Tasks.whenAllSuccess<Void>(copyTasks)
                                        .addOnSuccessListener {
                                            continuation.resume(Result.success(Unit))
                                        }
                                        .addOnFailureListener { error ->
                                            continuation.resumeWithException(error)
                                        }
                                } else {
                                    continuation.resume(Result.success(Unit))
                                }
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

    suspend fun moveSingleNoteToArchiveFirebase(noteId: String): Result<Unit> {
        return suspendCoroutine { continuation ->

            val noteRef = database.collection("Notes")
                .whereEqualTo("id", noteId)

            noteRef.get()
                .addOnSuccessListener { documents ->


                    for (document in documents) {

                        val archiveCollectionRef = database.collection("Archives")

                        val archiveNoteData = document.data

                        val archiveDocRef =
                            archiveCollectionRef.document(document.id)


                        archiveDocRef.set(archiveNoteData)
                            .addOnSuccessListener { archiveDocumentRef ->
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        val photoUrls =
                                            archiveNoteData["photo"] as? List<String> ?: emptyList()

                                        if (photoUrls.isNotEmpty()) {
                                            val copyTasks = mutableListOf<Task<Void>>()

                                            for (photoUrl in photoUrls) {
                                                val sourcePhotoReference =
                                                    storage.getReferenceFromUrl(photoUrl)
                                                val photoFileName = sourcePhotoReference.name
                                                val destPhotoReference =
                                                    storage.reference.child("archiveImages")
                                                        .child(photoFileName)

                                                val copyTask =
                                                    sourcePhotoReference.getBytes(Long.MAX_VALUE)
                                                        .addOnSuccessListener { sourceBytes ->
                                                            destPhotoReference.putBytes(sourceBytes)
                                                                .addOnSuccessListener {
                                                                    sourcePhotoReference.delete()
                                                                }
                                                        }
                                            }

                                            Tasks.whenAllSuccess<Void>(copyTasks)
                                                .addOnSuccessListener {
                                                    continuation.resume(Result.success(Unit))
                                                }
                                                .addOnFailureListener { error ->
                                                    continuation.resumeWithException(error)
                                                }
                                        } else {
                                            continuation.resume(Result.success(Unit))
                                        }
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
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }
}
