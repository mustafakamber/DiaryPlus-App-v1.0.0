package com.example.diarybook.service.DatabaseService

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudService {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()

    var currentUser = auth.currentUser

    suspend fun backloadAllDiariesFromFirebase() : Result<Unit>{
        return suspendCoroutine { continuation ->
            val tasks = mutableListOf<Task<*>>()

            database.collection("Backup")
                .whereEqualTo("user", currentUser!!.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val noteId = document.id
                        val noteData = document.data

                        val noteRef = database.collection("Notes").document(noteId)
                        val setNoteTask = noteData?.let { noteRef.set(it) }
                        if (setNoteTask != null) {
                            tasks.add(setNoteTask)
                        }

                        if (noteData != null) {
                            if (noteData.containsKey("photo")) {
                                val photoData = noteData["photo"]
                                if (photoData is List<*>) {
                                    val photoUrls = photoData as List<String>
                                    val photoFileNames = noteData["photoFileName"] as List<String>
                                    for (i in 0 until photoUrls.size) {
                                        val photoUrl = photoUrls[i]
                                        val photoFileName = photoFileNames[i]

                                        val photoRef = storage.getReferenceFromUrl(photoUrl)
                                        val pathFromNoteToBackup = photoRef.path.replace("noteImages/","backupImages/")
                                        val updatedPhotoRef = storage.getReference(pathFromNoteToBackup)

                                        val backupPhotoRef = storage.reference.child("noteImages")
                                            .child(photoFileName)

                                        val pathWithoutNoteImages = backupPhotoRef.path.replaceFirst("noteImages/","")
                                        val updatedBackupPhotoRef = storage.getReference(pathWithoutNoteImages)

                                        updatedPhotoRef.getBytes(Long.MAX_VALUE)
                                            .addOnSuccessListener { sourceBytes ->
                                                updatedBackupPhotoRef.putBytes(sourceBytes)
                                                    .addOnSuccessListener {
                                                        tasks.add(it.task)
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
                        }
                    }

                    Tasks.whenAll(tasks)
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

    suspend fun deleteAllBackupDataFromFirebase(): Result<Unit> {

        return suspendCoroutine { continuation ->
            val tasks = mutableListOf<Task<*>>()

            database.collection("Backup")
                .whereEqualTo("user", currentUser!!.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val deleteTask = database.collection("Backup").document(document.id).delete()
                        tasks.add(deleteTask)
                    }

                    Tasks.whenAll(tasks)
                        .addOnSuccessListener {
                            val storageRef = storage.reference.child("backupImages")
                            storageRef.listAll()
                                .addOnSuccessListener { result ->
                                    val deleteFileTasks = mutableListOf<Task<*>>()

                                    for (fileRef in result.items) {
                                        val deleteFileTask = fileRef.delete()
                                        deleteFileTasks.add(deleteFileTask)
                                    }

                                    Tasks.whenAll(deleteFileTasks)
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
                        .addOnFailureListener { error ->
                            continuation.resumeWithException(error)
                        }
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }


    suspend fun backupAllNotesFromFirebase(): Result<Unit> {
        return suspendCoroutine { continuation ->
            val tasks = mutableListOf<Task<*>>()

            database.collection("Notes")
                .whereEqualTo("user", currentUser!!.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val noteId = document.id
                        val noteData = document.data

                        val backupRef = database.collection("Backup").document(noteId)
                        val setNoteTask = noteData?.let { backupRef.set(it) }
                        if (setNoteTask != null) {
                            tasks.add(setNoteTask)
                        }

                        if (noteData != null) {
                            if (noteData.containsKey("photo")) {
                                val photoData = noteData["photo"]
                                if (photoData is List<*>) {
                                    val photoUrls = photoData as List<String>
                                    val photoFileNames = noteData["photoFileName"] as List<String>
                                    for (i in 0 until photoUrls.size) {
                                        val photoUrl = photoUrls[i]
                                        val photoFileName = photoFileNames[i]


                                        val photoRef = storage.getReferenceFromUrl(photoUrl)

                                        val backupPhotoRef = storage.reference.child("backupImages")
                                            .child(photoFileName)
                                        val pathWithoutNoteImages = backupPhotoRef.path.replace("noteImages/","")
                                        val updatedBackupPhotoRef = storage.getReference(pathWithoutNoteImages)


                                        photoRef.getBytes(Long.MAX_VALUE)
                                            .addOnSuccessListener { sourceBytes ->
                                                updatedBackupPhotoRef.putBytes(sourceBytes)
                                                    .addOnSuccessListener {
                                                        tasks.add(it.task)
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
                        }
                    }

                    Tasks.whenAll(tasks)
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