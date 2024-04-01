package com.example.diarybook.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.R
import com.example.diarybook.broadcast.Notification
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.githubAccount
import com.example.diarybook.constant.Constant.gmailAccount
import com.example.diarybook.constant.Constant.languageCodeEn
import com.example.diarybook.constant.Constant.languageCodeTr
import com.example.diarybook.databinding.FragmentSettingsBinding
import com.example.diarybook.util.setAppLanguage
import com.example.diarybook.view.activity.AuthActivity
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

    private lateinit var settingsFragmentBinding: FragmentSettingsBinding

    private lateinit var settingsViewModel: SettingsViewModel
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

        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        passwordViewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        accountDetails = AccountSheet(requireActivity(),requireContext(),settingsViewModel,passwordViewModel)
        myPhotos = MyPhotosSheet(requireContext(), settingsViewModel)
        confirmation = ConfirmationDialog(requireContext())
        database = DatabaseSheet(requireContext(), settingsViewModel)
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
        settingsFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings,container,false)
        return settingsFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLiveData()


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToHomeFragment()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

        setupSettingsScreen()

    }

    override fun onPause() {
        super.onPause()
        requireActivity().bottomAddNoteButton.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        requireActivity().bottomAddNoteButton.visibility = View.GONE
    }

    private fun setupSettingsScreen() = with(settingsFragmentBinding) {

        settingsViewModel.getUserInfoFromDB()

        requireActivity().bottomAddNoteButton.visibility = View.GONE

        notificationSettings.setOnClickListener {
            notificationSheet.getNotificationSheet(requireActivity())
        }

        languageSettings.setOnClickListener {

            languagePicker.getLanguageDialog({
                languageCodeTr.setAppLanguage(requireContext())
                settingsViewModel.saveStringData(LANGUAGE_DATA, languageCodeTr)
                reloadSettingsFragment()
            }, {
                languageCodeEn.setAppLanguage(requireContext())
                settingsViewModel.saveStringData(LANGUAGE_DATA, languageCodeEn)
                reloadSettingsFragment()
            })

        }


        /*
        val currentNightMode =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        modeSwitch.isChecked =
            currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES


        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Mevcut tema gündüzse geceye geç
                if (currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    reloadSettingsFragment()
                }
            } else {
                // Mevcut tema geceyse gündüze geç
                if (currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    reloadSettingsFragment()
                }
            }
        }

         */


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
                settingsViewModel.logOut {
                    getAuthActivityForLogout()
                }
            }

        }
    }

    private fun observeLiveData() = with(settingsFragmentBinding) {

        settingsViewModel.settingsToastMessage.observe(viewLifecycleOwner, Observer { error ->
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

        settingsViewModel.settingsSnackbarMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Snackbar.make(root,getString(message),Snackbar.LENGTH_SHORT).show()
            }
        })

        settingsViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            user?.let {

                settingsFragmentBinding.user = user

                accountSettings.setOnClickListener() {

                    accountDetails.getAccountSheet(user) { updatedName ->
                        settingsProfileNameText.text = updatedName
                    }

                }
            }
        })
    }

     private fun getAuthActivityForLogout(){
        val intentToAuthActivity = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intentToAuthActivity)
        requireActivity().finish()
    }

    private fun backToHomeFragment() {

        requireActivity().bottomMenuView.selectedItemId = R.id.homeButton

        val fragment = HomeFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment).commit()
        }
    }

    private fun reloadSettingsFragment() {
        val fragment = SettingsFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.baseFragment, fragment).commit()
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