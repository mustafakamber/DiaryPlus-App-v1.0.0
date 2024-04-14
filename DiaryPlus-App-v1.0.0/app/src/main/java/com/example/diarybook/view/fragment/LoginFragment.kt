package com.example.diarybook.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.diarybook.constant.Constant.NULL_STRING
import com.example.diarybook.constant.Constant.WEB_CLIENT_ID
import com.example.diarybook.databinding.FragmentLoginBinding
import com.example.diarybook.view.activity.BaseActivity
import com.example.diarybook.view.sheet.PasswordSheet
import com.example.diarybook.viewmodel.LoginViewModel
import com.example.diarybook.viewmodel.PasswordViewModel
import com.example.diarybook.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: FragmentLoginBinding
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
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        passwordViewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        resetPassword = PasswordSheet(requireContext(), passwordViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoginScreen()
        observeLiveData()
    }

    private fun setupLoginScreen() = with(binding) {
        loginEmailEdittext.addTextChangedListener {
            viewModel.setVisibleEmailEndIcon()
        }

        loginPasswordEdittext.addTextChangedListener {
            viewModel.setVisiblePasswordEndIcon()
        }

        loginButton.setOnClickListener {
            val emailAddress = loginEmailEdittext.text.toString().trim()
            val password = loginPasswordEdittext.text.toString().trim()
            val isCheckedRememberMe = loginRememberMeCheckBox.isChecked
            viewModel.inputControl(emailAddress, password, isCheckedRememberMe)
        }

        loginForgotPassword.setOnClickListener {
            resetPassword.getResetSheet(NULL_STRING)
        }

        loginWithGoogleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        loginCreateHereButton.setOnClickListener {
            val actionToSignup = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            findNavController().navigate(actionToSignup)
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val isCheckedRememberMe = binding.loginRememberMeCheckBox.isChecked
            viewModel.handleSignInGoogleRequest(result, isCheckedRememberMe)
        }

    private fun navigateHomeScreen() {
        val intentToBaseActivity = Intent(requireActivity(), BaseActivity::class.java)
        startActivity(intentToBaseActivity)
        requireActivity().finish()
    }

    private fun observeLiveData() = with(binding) {
        viewModel.rememberedEmail.observe(viewLifecycleOwner) { rememberedEmail ->
            rememberedEmail?.let {
                loginEmailEdittext.setText(rememberedEmail)
            }
        }

        viewModel.emailErrorText.observe(viewLifecycleOwner) { emailError ->
            emailError?.let {
                loginEmailEdittext.error = getString(emailError)
            }
        }

        viewModel.emailEndIconVisible.observe(viewLifecycleOwner) { emailVisibleBoolean ->
            emailVisibleBoolean?.let {
                loginEmailLayout.isEndIconVisible = emailVisibleBoolean
            }
        }

        viewModel.passwordErrorText.observe(viewLifecycleOwner) { passwordError ->
            passwordError?.let {
                loginPasswordEdittext.error = getString(passwordError)
            }
        }

        viewModel.passwordEndIconVisible.observe(viewLifecycleOwner)
        { passwordVisibleBoolean ->
            passwordVisibleBoolean?.let {
                loginPasswordLayout.isEndIconVisible = passwordVisibleBoolean
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.successLogin.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (success) {
                    navigateHomeScreen()
                }
            }
        }

        passwordViewModel.passwordToastMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.userToastMessage.observe(viewLifecycleOwner) { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

