package com.example.diarybook.viewmodel

import android.app.Activity
import android.app.Application
import androidx.activity.result.ActivityResult
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.model.User
import com.example.diarybook.service.AuthService
import com.example.diarybook.util.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class LoginViewModel (application: Application) : UserViewModel(application) {

    private val authService = AuthService()
    private val sharedPreferences = SharedPreferences(getApplication())
    val loginToastMessage = MutableLiveData<String>()

    fun saveStringData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences.saveBooleanData(key, value)
    }

    fun getStringData(key: String): String? {
        return sharedPreferences.getStringData(key)
    }

    fun deleteData(key: String) {
        sharedPreferences.deleteData(key)
    }

    fun handleSignInGoogleRequest(result: ActivityResult, onSuccess: () -> Unit, ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task, onSuccess)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>, onSuccess: () -> Unit) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                logInGoogleAccount(account) {
                    createNewUser(account) {
                        onSuccess()
                    }
                }
            }
        } else {
            loginToastMessage.value = task.exception.toString()
        }
    }

    private fun logInGoogleAccount(account: GoogleSignInAccount, onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.signInWithCredential(account)
                }
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    loginToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private fun createNewUser(account: GoogleSignInAccount, onSuccess: () -> Unit) {

        val newUser = User(
            account.email.toString(),
            account.displayName.toString(),
            account.photoUrl.toString()
        )

        addNewUserOrUpdateToDB(newUser) {
            onSuccess()
        }

    }

    fun logIn(email: String, password: String, onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.signInWithEmailAndPassword(email, password)
                }
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    loginToastMessage.value = error.localizedMessage
                }
            }
        }
    }

}