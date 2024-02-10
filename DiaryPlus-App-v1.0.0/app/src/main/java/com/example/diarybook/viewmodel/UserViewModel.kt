package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.model.User
import com.example.diarybook.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class UserViewModel (application: Application) : CoroutineViewModel(application) {

    private val authService = AuthService()
    val userToastMessage = MutableLiveData<String>()

    fun currentUser(onSuccess: () -> Unit,onCancel : () -> Unit){
        authService.currentUserController(onSuccess,onCancel)
    }

    fun addNewUserOrUpdateToDB(user: User, onSuccess: () -> Unit) {

        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.createNewUser(user)
                }
                onSuccess()
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    userToastMessage.value = error.localizedMessage
                }
            }
        }
    }

}