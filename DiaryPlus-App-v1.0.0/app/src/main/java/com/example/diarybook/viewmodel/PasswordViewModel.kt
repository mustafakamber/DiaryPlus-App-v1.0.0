package com.example.diarybook.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordViewModel (application: Application) : CoroutineViewModel(application) {

    private val authService = AuthService()
    val passwordToastMessage = MutableLiveData<String>()

    fun resetPassword(email: String, operationComplete: () -> Unit, onSendNotComplete : () -> Unit){
        launch {
            try {
                withContext(Dispatchers.IO){
                    authService.checkEmailToSend(email,onSendNotComplete)
                }
                operationComplete()
            }catch (error : Exception){
                withContext(Dispatchers.Main) {
                    passwordToastMessage.value =  error.localizedMessage
                }
            }
        }
    }
}