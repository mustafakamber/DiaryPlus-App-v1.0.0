package com.example.diarybook.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.diarybook.R.string
import com.example.diarybook.databinding.FragmentLoginBinding
import com.example.diarybook.util.*
import com.example.diarybook.util.Constant.EMAIL_DATA
import com.example.diarybook.util.Constant.WEB_CLIENT_ID
import com.example.diarybook.util.Constant.nullEmail
import com.example.diarybook.view.activity.BaseActivity
import com.example.diarybook.view.sheet.PasswordSheet
import com.example.diarybook.viewmodel.LoginViewModel
import com.example.diarybook.viewmodel.PasswordViewModel
import com.example.diarybook.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var userViewModel : UserViewModel

    private lateinit var loginFragmentBinding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var resetPassword : PasswordSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        passwordViewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        resetPassword = PasswordSheet(requireContext(), passwordViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loginFragmentBinding = FragmentLoginBinding.inflate(layoutInflater)
        return loginFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(loginFragmentBinding) {
        super.onViewCreated(view, savedInstanceState)

        observeLiveData()

        val rememberedEmail = loginViewModel.getStringData(EMAIL_DATA)


        if (!(TextUtils.isEmpty(loginEmailEdittext.toString()))) {
            loginEmailEdittext.setText(rememberedEmail.toString())
        } else {
            loginEmailEdittext.setText("")
        }

        loginButton.setOnClickListener {


            val emailAddress = loginEmailEdittext.text.toString().trim()
            val password = loginPasswordEdittext.text.toString().trim()

            nullInputControl(emailAddress, password) {
                logInRequest(emailAddress, password)
            }

        }

        loginForgotPassword.setOnClickListener {
            resetPassword.getResetSheet(nullEmail)
        }

        loginWithGoogleButton.setOnClickListener {
            logInWithGoogleRequest()
        }

        loginCreateHereButton.setOnClickListener {
            getSignupFragmentForCreateHere()
        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun nullInputControl(emailAddress: String, password: String, onSuccess: () -> Unit) =
        with(loginFragmentBinding) {

            if(TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)){
                if (TextUtils.isEmpty(emailAddress)) {
                    loginEmailLayout.isEndIconVisible = false
                    loginEmailEdittext.error = getString(string.enter_email_error_message)
                    loginEmailEdittext.addTextChangedListener {
                        loginEmailLayout.isEndIconVisible = true
                    }
                }
                if (TextUtils.isEmpty(password)) {
                    loginPasswordLayout.isEndIconVisible = false
                    loginPasswordEdittext.error = getString(string.enter_password_error_message)
                    loginPasswordEdittext.addTextChangedListener {
                        loginPasswordLayout.isEndIconVisible = true
                    }
                }
                return
            }else{
                onSuccess()
            }
        }

    private fun logInWithGoogleRequest() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            loginViewModel.handleSignInGoogleRequest(result) {
                if (!loginFragmentBinding.loginRememberMeCheckBox.isChecked) {
                    loginViewModel.deleteData(EMAIL_DATA)
                }
                getBaseActivityForLogin()
            }
        }

    private fun logInRequest(emailAddress: String, password: String) {
        loginViewModel.logIn(emailAddress, password) {
            if (loginFragmentBinding.loginRememberMeCheckBox.isChecked) {
                loginViewModel.saveStringData(EMAIL_DATA, emailAddress)
            } else {
                loginViewModel.deleteData(EMAIL_DATA)
            }
            getBaseActivityForLogin()
        }
    }

    private fun getBaseActivityForLogin() {

        val intentToBaseActivity = Intent(requireActivity(),BaseActivity::class.java)
        startActivity(intentToBaseActivity)
        requireActivity().finish()

    }

    private fun getSignupFragmentForCreateHere() {
        findNavController()
            .navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
    }

    private fun observeLiveData() = with(loginFragmentBinding) {

        loginViewModel.loginToastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(),toastMessage, Toast.LENGTH_SHORT).show()
            }
        })
        passwordViewModel.passwordToastMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(),error, Toast.LENGTH_SHORT).show()
            }
        })
        userViewModel.userToastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(),toastMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

