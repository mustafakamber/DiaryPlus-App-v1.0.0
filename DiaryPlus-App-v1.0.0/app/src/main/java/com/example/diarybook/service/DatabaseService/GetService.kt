package com.example.diarybook.service.DatabaseService

import android.net.Uri
import com.example.diarybook.model.Diary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GetService {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()

    var currentUser = auth.currentUser

    suspend fun getAllPhotosFromFirebase(): Result<MutableList<Uri>> {
        return suspendCoroutine { continuation ->
            database.collection("Notes")
                .whereEqualTo("user", currentUser!!.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val photoList = mutableListOf<Uri>()

                    for (document in querySnapshot) {
                        val noteData = document.data
                        val notePhotosString = noteData["photo"] as List<String>?

                        if (notePhotosString != null) {
                            val notePhotos = notePhotosString.map { Uri.parse(it) }.toMutableList()
                            photoList.addAll(notePhotos)
                        }
                    }
                    continuation.resume(Result.success(photoList))
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }


    suspend fun getAllNotesFromFirebase(): Result<ArrayList<Diary>> {
        return suspendCoroutine { continuation ->

            database.collection("Notes")
                .whereEqualTo("user", currentUser!!.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val notesList = ArrayList<Diary>()

                    for (document in querySnapshot) {

                        val noteData = document.data
                        val noteId = noteData["id"] as String
                        val noteTitle = noteData["title"] as String
                        val noteContent = noteData["content"] as String
                        val noteBackground = noteData["background color"] as String
                        val noteText = noteData["text color"] as String
                        val noteDateEn = noteData["date en"] as String
                        val noteDateTr = noteData["date tr"] as String
                        val notePhotosString = noteData["photo"] as List<String>? ?: emptyList()
                        val noteTime = noteData["time"] as String


                        val notePhotos = notePhotosString.map { Uri.parse(it) }.toMutableList()


                        val diary = Diary(
                            noteId,
                            noteTitle,
                            noteContent,
                            notePhotos,
                            noteDateEn,
                            noteDateTr,
                            noteBackground,
                            noteText,
                            noteTime
                        )

                        notesList.add(diary)

                    }
                    continuation.resume(Result.success(notesList))
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }

    suspend fun getCalendarNotesFromFirebase(noteDate: String): Result<ArrayList<Diary>> {
        return suspendCoroutine { continuation ->
            database.collection("Notes")
                .whereEqualTo("user", currentUser!!.email.toString())
                .whereEqualTo("date en", noteDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val notesList = ArrayList<Diary>()

                    for (document in querySnapshot) {

                        val noteData = document.data
                        val noteId = noteData["id"] as String
                        val noteTitle = noteData["title"] as String
                        val noteContent = noteData["content"] as String
                        val noteBackground = noteData["background color"] as String
                        val noteText = noteData["text color"] as String
                        val noteDateEn = noteData["date en"] as String
                        val noteDateTr = noteData["date tr"] as String
                        val notePhotosString = noteData["photo"] as List<String>? ?: emptyList()
                        val noteTime = noteData["time"] as String

                        val notePhotos = notePhotosString.map { Uri.parse(it) }.toMutableList()

                        val diary = Diary(
                            noteId,
                            noteTitle,
                            noteContent,
                            notePhotos,
                            noteDateEn,
                            noteDateTr,
                            noteBackground,
                            noteText,
                            noteTime
                        )

                        notesList.add(diary)
                    }
                    continuation.resume(Result.success(notesList))
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }

        }
    }

    suspend fun getAllArchiveNotesFromFirebase(): Result<ArrayList<Diary>> {
        return suspendCoroutine { continuation ->

            database.collection("Archives")
                .whereEqualTo("user", currentUser!!.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val archiveList = ArrayList<Diary>()

                    for (document in querySnapshot) {
                        val archiveData = document.data
                        val archiveId = archiveData["id"] as String
                        val archiveTitle = archiveData["title"] as String
                        val archiveContent = archiveData["content"] as String
                        val archiveBackground = archiveData["background color"] as String
                        val archiveText = archiveData["text color"] as String
                        val archiveDateEn = archiveData["date en"] as String
                        val archiveDateTr = archiveData["date tr"] as String
                        val archivePhotosString =
                            archiveData["photo"] as List<String>? ?: emptyList()
                        val archiveTime = archiveData["time"] as String

                        val archivePhotos =
                            archivePhotosString.map { Uri.parse(it) }.toMutableList()

                        val archiveDiary = Diary(
                            archiveId,
                            archiveTitle,
                            archiveContent,
                            archivePhotos,
                            archiveDateEn,
                            archiveDateTr,
                            archiveBackground,
                            archiveText,
                            archiveTime
                        )

                        archiveList.add(archiveDiary)

                    }
                    continuation.resume(Result.success(archiveList))
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }

    suspend fun getSingleNoteFromFirebase(noteId: String): Result<Diary?> {
        return suspendCoroutine { continuation ->
            database.collection("Notes")
                .whereEqualTo("id", noteId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        continuation.resume(Result.success(null))
                    } else {

                        val document = querySnapshot.documents[0]
                        val noteData = document.data

                        val noteTitle = noteData?.get("title") as? String ?: ""
                        val noteContent = noteData?.get("content") as? String ?: ""
                        val noteBackground = noteData?.get("background color") as? String ?: ""
                        val noteText = noteData?.get("text color") as? String ?: ""
                        val noteDateEn = noteData?.get("date en") as? String ?: ""
                        val noteDateTr = noteData?.get("date tr") as? String ?: ""
                        val notePhotosString = noteData?.get("photo") as? List<String> ?: emptyList()
                        val noteTime = noteData?.get("time") as? String ?: ""

                        val notePhotos = notePhotosString.map { Uri.parse(it) }.toMutableList()

                        val diary = Diary(
                            noteId,
                            noteTitle,
                            noteContent,
                            notePhotos,
                            noteDateEn,
                            noteDateTr,
                            noteBackground,
                            noteText,
                            noteTime
                        )

                        continuation.resume(Result.success(diary))
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
        }
    }
}