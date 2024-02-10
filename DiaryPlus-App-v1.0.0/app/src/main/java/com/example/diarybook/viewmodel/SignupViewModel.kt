package com.example.diarybook.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.model.User
import com.example.diarybook.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupViewModel (application: Application) : UserViewModel(application) {

    private val authService = AuthService()
    val signupToastMessage = MutableLiveData<String>()


    fun signUp(activity: Activity, email: String, password: String, onSuccess: () -> Unit) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.createUserWithEmailAndPassword(email, password)
                }
                createNewUser(activity, email, onSuccess)
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    signupToastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private fun createNewUser(activity: Activity, emailAddress: String, onSuccess: () -> Unit) {

        val defaultProfileName = activity.getString(R.string.profile_name_default)

        val newUser = User(
            emailAddress,
            defaultProfileName,
            null
        )

        addNewUserOrUpdateToDB(newUser) {

            onSuccess()

        }
    }

}