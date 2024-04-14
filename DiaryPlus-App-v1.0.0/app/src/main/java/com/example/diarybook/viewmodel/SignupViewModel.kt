package com.example.diarybook.viewmodel

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.model.User
import com.example.diarybook.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupViewModel (application: Application) : UserViewModel(application) {

    private val authService = AuthService()
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val _successSignup = MutableLiveData<Boolean>()
    val successSignup: LiveData<Boolean>
        get() = _successSignup

    private val _emailErrorText = MutableLiveData<Int>()
    val emailErrorText: LiveData<Int>
        get() = _emailErrorText

    private val _emailEndIconVisible = MutableLiveData<Boolean>()
    val emailEndIconVisible: LiveData<Boolean>
        get() = _emailEndIconVisible

    private val _passwordErrorText = MutableLiveData<Int>()
    val passwordErrorText: LiveData<Int>
        get() = _passwordErrorText

    private val _passwordEndIconVisible = MutableLiveData<Boolean>()
    val passwordEndIconVisible: LiveData<Boolean>
        get() = _passwordEndIconVisible

    private val _confirmPasswordErrorText = MutableLiveData<Int>()
    val confirmPasswordErrorText: LiveData<Int>
        get() = _confirmPasswordErrorText

    private val _confirmPasswordEndIconVisible = MutableLiveData<Boolean>()
    val confirmPasswordEndIconVisible: LiveData<Boolean>
        get() = _confirmPasswordEndIconVisible

    fun setVisibleEmailEndIcon() {
        _emailEndIconVisible.value = true
    }

    fun setVisiblePasswordEndIcon() {
        _passwordEndIconVisible.value = true
    }

    fun setVisibleConfirmPasswordEndIcon() {
        _confirmPasswordEndIconVisible.value = true
    }

    fun inputControl(
        emailAddress: String,
        password: String,
        confirmPassword: String,
        context: Context
    ) {
        if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password) || TextUtils.isEmpty(
                confirmPassword
            )
        ) {
            if (TextUtils.isEmpty(emailAddress)) {
                _emailEndIconVisible.value = false
                _emailErrorText.value = R.string.enter_email_error_message
            }
            if (TextUtils.isEmpty(password)) {
                _passwordEndIconVisible.value = false
                _passwordErrorText.value = R.string.enter_password_error_message
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                _confirmPasswordEndIconVisible.value = false
                _confirmPasswordErrorText.value = R.string.enter_password_error_message
            }
            return
        } else if (confirmPassword != password) {
            _toastMessage.value = context.getString(R.string.password_notmatch_error_message)
            return
        } else if (password.length >= 16) {
            _passwordEndIconVisible.value = false
            _passwordErrorText.value = R.string.password_overlimit_error_message
            return
        } else {
            signUp(context, emailAddress, password)
        }
    }

    private fun signUp(context: Context, email: String, password: String) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.createUserWithEmailAndPassword(email, password)
                }
                createNewUser(context, email)
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private fun createNewUser(context: Context, emailAddress: String) {
        val defaultProfileName = context.getString(R.string.profile_name_default)
        val newUser = User(
            emailAddress,
            defaultProfileName,
            null
        )
        addNewUserOrUpdateToDB(newUser) {
            _successSignup.value = true
        }
    }
}