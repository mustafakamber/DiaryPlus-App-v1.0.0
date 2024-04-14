package com.example.diarybook.viewmodel

import android.app.Activity
import android.app.Application
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.EMAIL_DATA
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

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

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

    private val _rememberedEmail = MutableLiveData<String>()
    val rememberedEmail: LiveData<String>
        get() = _rememberedEmail

    private val _successLogin = MutableLiveData<Boolean>()
    val successLogin: LiveData<Boolean>
        get() = _successLogin

    init {
        getRememberedEmail()
    }

    private fun saveStringData(key: String, value: String) {
        sharedPreferences.saveStringData(key, value)
    }

    private fun getStringData(key: String): String? {
        return sharedPreferences.getStringData(key)
    }

    private fun deleteData(key: String) {
        sharedPreferences.deleteData(key)
    }

    private fun getRememberedEmail() {
        getStringData(EMAIL_DATA)?.let {
            _rememberedEmail.postValue(it)
        }
    }

    fun setVisibleEmailEndIcon() {
        _emailEndIconVisible.value = true
    }

    fun setVisiblePasswordEndIcon() {
        _passwordEndIconVisible.value = true
    }

    fun inputControl(emailAddress: String, password: String, isRememberMeChecked: Boolean) {
        if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(emailAddress)) {
                _emailEndIconVisible.value = false
                _emailErrorText.value = R.string.enter_email_error_message
            }
            if (TextUtils.isEmpty(password)) {
                _passwordEndIconVisible.value = false
                _passwordErrorText.value = R.string.enter_password_error_message
            }
            return
        } else {
            logIn(emailAddress, password, isRememberMeChecked)
        }
    }

    private fun logIn(email: String, password: String, isRememberMeChecked: Boolean) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.signInWithEmailAndPassword(email, password)
                }
                if (isRememberMeChecked) {
                    saveStringData(EMAIL_DATA, email)
                } else {
                    deleteData(EMAIL_DATA)
                }
                _successLogin.value = true
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
                }
            }
        }
    }

    fun handleSignInGoogleRequest(result: ActivityResult, isRememberMeChecked: Boolean) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task, isRememberMeChecked)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>, isRememberMeChecked: Boolean) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                logInGoogleAccount(account, isRememberMeChecked)
            }
        } else {
            _toastMessage.value = task.exception.toString()
        }
    }

    private fun logInGoogleAccount(account: GoogleSignInAccount, isRememberMeChecked: Boolean) {
        launch {
            try {
                withContext(Dispatchers.IO) {
                    authService.signInWithCredential(account)
                }
                createNewUser(account, isRememberMeChecked)
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = error.localizedMessage
                }
            }
        }
    }

    private fun createNewUser(account: GoogleSignInAccount, isRememberMeChecked: Boolean) {
        val newUser = User(
            account.email.toString(),
            account.displayName.toString(),
            account.photoUrl.toString()
        )
        addNewUserOrUpdateToDB(newUser) {
            if (!isRememberMeChecked) {
                deleteData(EMAIL_DATA)
            }
            _successLogin.value = true
        }
    }
}