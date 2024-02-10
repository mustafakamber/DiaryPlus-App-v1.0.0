package com.example.diarybook.service


import com.example.diarybook.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthService {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()

    var currentUser = auth.currentUser

    fun currentUserController(onSuccess: () -> Unit, onCancel: () -> Unit) {
        if (currentUser != null) {
            onSuccess()
        } else {
            onCancel()
        }
    }

    suspend fun currentUserLogOut(): Result<Unit> {
        return suspendCoroutine { continuation ->
            auth.signOut()
            continuation.resume(Result.success(Unit))
        }
    }

    suspend fun currentUserDeleteAccount(): Result<Unit> {
        return suspendCoroutine { continuation ->

            if (currentUser != null){
                val userRef = database.collection("Users")
                    .whereEqualTo("email",currentUser!!.email)

                userRef.get()
                    .addOnSuccessListener { documents ->
                        for (document in documents){
                            document.reference.delete()
                                .addOnSuccessListener {
                                    currentUser!!.delete()
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
                    .addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }
            }

        }
    }

    suspend fun getUserInfo() : Result<User>{

        return suspendCoroutine { continuation ->

            val userUid = currentUser?.uid

            if (userUid != null){
                val userDocRef = database.collection("Users")
                    .document(userUid)

                userDocRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        val userMap = documentSnapshot.data

                        val email = userMap?.get("email").toString()
                        val name = userMap?.get("name") as? String ?: ""
                        val image = userMap?.get("image") as? String ?: ""


                        val user = User(email,name,image)

                        continuation.resume(Result.success(user))
                    }
                    .addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }
            }

        }

    }

    suspend fun createNewUser(user : User) : Result<Unit>{

        return suspendCoroutine { continuation ->
            val currentUser = auth.currentUser
            val userUid = currentUser?.uid

            if(userUid != null){

                val userDocRef = database.collection("Users").document(userUid)

                val userMap = hashMapOf<String,Any>()
                userMap["email"] = user.userEmail
                user.userName.let {
                    userMap["name"] = user.userName.toString()
                }
                user.userImage.let {
                    userMap["image"] = user.userImage.toString()
                }

                userDocRef.set(userMap)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }

            }
        }
    }

    suspend fun checkEmailToSend(email: String,onSendNotCompelete : () -> Unit): Result<Unit> {
        return suspendCoroutine { continuation ->
            if (email != null) {
                val userDocRef = database.collection("Users")
                    .whereEqualTo("email", email)

                userDocRef.get()
                    .addOnSuccessListener { documents ->
                        if (documents.size() == 0) {
                            onSendNotCompelete()
                        } else {
                            auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener {
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
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String)
            : Result<Unit> {
        return suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }.addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }

    suspend fun signInWithCredential(account: GoogleSignInAccount)
            : Result<Unit> {
        return suspendCoroutine { continuation ->
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }.addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }



    suspend fun createUserWithEmailAndPassword(email: String, password: String)
            : Result<Unit> {
        return suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(
                email,
                password
            )
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }.addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }


}