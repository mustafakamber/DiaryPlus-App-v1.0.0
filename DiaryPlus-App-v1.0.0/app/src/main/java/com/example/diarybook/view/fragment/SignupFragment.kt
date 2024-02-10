package com.example.diarybook.view.fragment

import android.content.Intent
import com.example.diarybook.viewmodel.SignupViewModel
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.diarybook.R
import com.example.diarybook.databinding.FragmentSignupBinding
import com.example.diarybook.view.activity.BaseActivity
import com.example.diarybook.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.fragment_signup.*

class SignupFragment : Fragment() {

    private lateinit var signupViewModel: SignupViewModel
    private lateinit var userViewModel : UserViewModel

    private lateinit var signupFragmentBinding: FragmentSignupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signupViewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        signupFragmentBinding = FragmentSignupBinding.inflate(layoutInflater)

        return signupFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(signupFragmentBinding) {
        super.onViewCreated(view, savedInstanceState)


        observeLiveData()

        signupButton.setOnClickListener {

            val (emailAddress, password, confirmPassword) = getUserSignupInput()




            inputAndPasswordControl(emailAddress, password, confirmPassword) {
                signUpRequest(emailAddress, password)
            }

        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

        signupLoginHereButton.setOnClickListener {
            getLoginFragmentForLoginHere()
        }

    }

    private fun inputAndPasswordControl(
        emailAddress: String, password: String, confirmPassword: String, onSuccess: () -> Unit
    ) = with(signupFragmentBinding) {


        if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)){

            if (TextUtils.isEmpty(emailAddress)) {
                signupEmailLayout.isEndIconVisible = false
                signupEmailEdittext.error = getString(R.string.enter_email_error_message)
                signupEmailEdittext.addTextChangedListener {
                    signupEmailLayout.isEndIconVisible = true
                }
            }

            if (TextUtils.isEmpty(password)) {
                signupPasswordLayout.isEndIconVisible = false
                signupPasswordEdittext.error = getString(R.string.enter_password_error_message)
                signupPasswordEdittext.addTextChangedListener {
                    signupPasswordLayout.isEndIconVisible = true
                }
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                signupConfirmPasswordLayout.isEndIconVisible = false
                signupConfirmPasswordEdittext.error = getString(R.string.enter_confirm_password_error_message)
                signupConfirmPasswordEdittext.addTextChangedListener {
                    signupConfirmPasswordLayout.isEndIconVisible = true
                }
            }

            return
        }else if (confirmPassword != password) {
            signupViewModel.signupToastMessage.value = getString(R.string.password_notmatch_error_message)
            return
        }
        else if (password.length >= 16) {

            signupPasswordLayout.isEndIconVisible = false
            signupPasswordEdittext.error = getString(R.string.password_overlimit_error_message)
            signupPasswordEdittext.addTextChangedListener {
                signupPasswordLayout.isEndIconVisible = true
            }

            return
        }else{
            onSuccess()
        }

    }

    private fun signUpRequest(emailAddress: String, password: String) {
        signupViewModel.signUp(requireActivity(), emailAddress, password) {
            getBaseActivityForSignup()
        }
    }

    private fun getBaseActivityForSignup() {
        val intentToBaseActivity = Intent(requireActivity(), BaseActivity::class.java)
        startActivity(intentToBaseActivity)
        requireActivity().finish()
    }

    private fun getLoginFragmentForLoginHere() {
        findNavController()
            .navigate(SignupFragmentDirections.actionSignupFragmentToLoginFragment())
    }

    private fun getUserSignupInput(): Triple<String, String, String> {

        val email = signupEmailEdittext.text.toString().trim()
        val password = signupPasswordEdittext.text.toString().trim()
        val confirmPassword = signupConfirmPasswordEdittext.text.toString().trim()

        return Triple(email, password, confirmPassword)
    }

    private fun observeLiveData() = with(signupFragmentBinding) {

        signupViewModel.signupToastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(),toastMessage,Toast.LENGTH_SHORT).show()
            }
        })
        userViewModel.userToastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(),toastMessage,Toast.LENGTH_SHORT).show()
            }
        })

    }

}