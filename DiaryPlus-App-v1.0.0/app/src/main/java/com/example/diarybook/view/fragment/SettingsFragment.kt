package com.example.diarybook.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.R
import com.example.diarybook.broadcast.Notification
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.MODE_DATA
import com.example.diarybook.constant.Constant.THEME_KEY
import com.example.diarybook.constant.Constant.githubAccount
import com.example.diarybook.constant.Constant.gmailAccount
import com.example.diarybook.constant.Constant.languageCodeEn
import com.example.diarybook.constant.Constant.languageCodeTr
import com.example.diarybook.databinding.FragmentSettingsBinding
import com.example.diarybook.util.checkAppTheme
import com.example.diarybook.util.setAppLanguage
import com.example.diarybook.util.setDarkTheme
import com.example.diarybook.util.setLightTheme
import com.example.diarybook.view.activity.AuthActivity
import com.example.diarybook.view.activity.BaseActivity
import com.example.diarybook.view.dialog.ConfirmationDialog
import com.example.diarybook.view.dialog.LanguageDialog
import com.example.diarybook.view.sheet.AccountSheet
import com.example.diarybook.view.sheet.DatabaseSheet
import com.example.diarybook.view.sheet.MyPhotosSheet
import com.example.diarybook.view.sheet.NotificationSheet
import com.example.diarybook.view.sheet.PasswordSheet
import com.example.diarybook.viewmodel.PasswordViewModel
import com.example.diarybook.viewmodel.SettingsViewModel
import com.example.diarybook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_base.bottomAddNoteButton
import kotlinx.android.synthetic.main.activity_base.bottomMenuView


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var myPhotos: MyPhotosSheet
    private lateinit var database: DatabaseSheet
    private lateinit var confirmation: ConfirmationDialog
    private lateinit var notificationSheet: NotificationSheet
    private lateinit var notificationService: Notification
    private lateinit var languagePicker: LanguageDialog
    private lateinit var accountDetails: AccountSheet
    private lateinit var resetPassword: PasswordSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        passwordViewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        accountDetails = AccountSheet(requireActivity(),requireContext(),viewModel,passwordViewModel)
        myPhotos = MyPhotosSheet(requireContext(), viewModel)
        confirmation = ConfirmationDialog(requireContext())
        database = DatabaseSheet(requireContext(), viewModel)
        notificationSheet = NotificationSheet(requireContext())
        notificationService = Notification()
        languagePicker = LanguageDialog(requireContext(), requireActivity())
        resetPassword = PasswordSheet(requireContext(),passwordViewModel)

        notificationService.createNotificationChannel(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettingsScreen()
        observeLiveData()
    }

    private fun setupSettingsScreen() = with(binding) {
        viewModel.getUserInfoFromDB()

        notificationSettings.setOnClickListener {
            notificationSheet.getNotificationSheet(requireActivity())
        }

        languageSettings.setOnClickListener {
            languagePicker.getLanguageDialog({
                languageCodeTr.setAppLanguage(requireContext())
                viewModel.saveStringData(LANGUAGE_DATA, languageCodeTr)
                reloadSettingsScreen()
            }, {
                languageCodeEn.setAppLanguage(requireContext())
                viewModel.saveStringData(LANGUAGE_DATA, languageCodeEn)
                reloadSettingsScreen()
            })
        }

        checkAppTheme(requireContext(),{
            themeSwitch.isChecked = false
            setLightTheme(requireContext())
        },{
            themeSwitch.isChecked = true
            setDarkTheme(requireContext())
        })

        val nightMode = viewModel.getBooleanData(MODE_DATA)
        themeSwitch.setOnClickListener {
           setAppTheme(nightMode)
        }

        photoSettings.setOnClickListener {
            myPhotos.getMyPhotosSheet()
        }

        databaseSettings.setOnClickListener {
            database.getDatabaseSheet()
        }

        aboutSettings.setOnClickListener {
            openUrlOrSendEmail(githubAccount)
        }

        messageSettings.setOnClickListener {
            openUrlOrSendEmail(gmailAccount)
        }

        logoutSettings.setOnClickListener {
            confirmation.getConfirmationDialog(
                getString(R.string.logout_message)
            ) {
                viewModel.logOut {
                    navigateLoginScreen()
                }
            }
        }
    }

    private fun observeLiveData() = with(binding) {
        viewModel.settingsToastMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        })

        passwordViewModel.passwordToastMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(),error, Toast.LENGTH_SHORT).show()
            }
        })

        userViewModel.userToastMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(),error, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.settingsSnackbarMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Snackbar.make(root,getString(message),Snackbar.LENGTH_SHORT).show()
            }
        })

        viewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                binding.user = user
                accountSettings.setOnClickListener {
                    accountDetails.getAccountSheet(user) { updatedName ->
                        settingsProfileNameText.text = updatedName
                    }
                }
            }
        })
    }

    private fun setAppTheme(isNightMode : Boolean){
        if (isNightMode){
            setLightTheme(requireContext())
        }else{
            setDarkTheme(requireContext())
        }
        viewModel.saveBooleanData(THEME_KEY,true)
    }

     private fun navigateLoginScreen(){
        val intentToAuthActivity = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intentToAuthActivity)
        requireActivity().finish()
    }

    private fun reloadSettingsScreen() {
        val fragment = SettingsFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment)
            commit()
        }
    }

    private fun openUrlOrSendEmail(urlOrEmail: String) {
        val intent: Intent
        if (urlOrEmail.startsWith("http") || urlOrEmail.startsWith("https")) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlOrEmail))
        } else if (urlOrEmail.contains("@")) {
            intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$urlOrEmail")
        } else {
            return
        }
        startActivity(intent)
    }
}