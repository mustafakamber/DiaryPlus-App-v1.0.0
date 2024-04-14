package com.example.diarybook.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.diarybook.databinding.FragmentSignupBinding
import com.example.diarybook.view.activity.BaseActivity
import com.example.diarybook.viewmodel.SignupViewModel
import com.example.diarybook.viewmodel.UserViewModel

class SignupFragment : Fragment() {

    private lateinit var viewModel: SignupViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: FragmentSignupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
        setupSignupScreen()
    }

    private fun setupSignupScreen() = with(binding) {
        signupEmailEdittext.addTextChangedListener {
            viewModel.setVisibleEmailEndIcon()
        }

        signupPasswordEdittext.addTextChangedListener {
            viewModel.setVisiblePasswordEndIcon()
        }

        signupConfirmPasswordEdittext.addTextChangedListener {
            viewModel.setVisibleConfirmPasswordEndIcon()
        }

        signupButton.setOnClickListener {
            val email = signupEmailEdittext.text.toString().trim()
            val password = signupPasswordEdittext.text.toString().trim()
            val confirmPassword = signupConfirmPasswordEdittext.text.toString().trim()
            viewModel.inputControl(email, password, confirmPassword, requireContext())
        }

        signupLoginHereButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeLiveData() = with(binding) {
        viewModel.emailErrorText.observe(viewLifecycleOwner) { emailError ->
            emailError?.let {
                signupEmailEdittext.error = getString(emailError)
            }
        }

        viewModel.emailEndIconVisible.observe(viewLifecycleOwner) { emailVisibleBoolean ->
            emailVisibleBoolean?.let {
                signupEmailLayout.isEndIconVisible = emailVisibleBoolean
            }
        }

        viewModel.passwordErrorText.observe(viewLifecycleOwner) { passwordError ->
            passwordError?.let {
                signupPasswordEdittext.error = getString(passwordError)
            }
        }

        viewModel.passwordEndIconVisible.observe(viewLifecycleOwner)
        { passwordVisibleBoolean ->
            passwordVisibleBoolean?.let {
                signupPasswordLayout.isEndIconVisible = passwordVisibleBoolean
            }
        }

        viewModel.confirmPasswordErrorText.observe(viewLifecycleOwner) { confirmPasswordError ->
            confirmPasswordError?.let {
                signupConfirmPasswordEdittext.error = getString(confirmPasswordError)
            }
        }

        viewModel.confirmPasswordEndIconVisible.observe(viewLifecycleOwner)
        { confirmPasswordVisibleBoolean ->
            confirmPasswordVisibleBoolean?.let {
                signupConfirmPasswordLayout.isEndIconVisible = confirmPasswordVisibleBoolean
            }
        }

        viewModel.successSignup.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (success) {
                    navigateHomeScreen()
                }
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.userToastMessage.observe(viewLifecycleOwner) { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateHomeScreen() {
        val intentToBaseActivity = Intent(requireActivity(), BaseActivity::class.java)
        startActivity(intentToBaseActivity)
        requireActivity().finish()
    }
}