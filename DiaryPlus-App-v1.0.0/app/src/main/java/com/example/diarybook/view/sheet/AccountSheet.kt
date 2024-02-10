package com.example.diarybook.view.sheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.diarybook.R
import com.example.diarybook.databinding.BottomSheetAccountBinding
import com.example.diarybook.model.User
import com.example.diarybook.view.activity.AuthActivity
import com.example.diarybook.view.dialog.ConfirmationDialog
import com.example.diarybook.view.dialog.NameDialog
import com.example.diarybook.viewmodel.PasswordViewModel
import com.example.diarybook.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class AccountSheet
    (
    private val activity : Activity,
    private val context: Context,
    private val settingsViewModel : SettingsViewModel,
    private val passwordViewModel : PasswordViewModel,
    ) {

    private lateinit var accountBottomSheet: BottomSheetDialog


    val confirmation = ConfirmationDialog(context)
    val resetPassword = PasswordSheet(context,passwordViewModel)
    val nameChanger = NameDialog(activity,context,settingsViewModel)

    fun getAccountSheet(
        user : User,
        changeSettingsProfileName : (String) -> Unit,
    ) {

        accountBottomSheet = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)

        val bottomSheetView = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_account,
            null
        )

        val binding = BottomSheetAccountBinding.bind(bottomSheetView)


        val accountBackButton = bottomSheetView.findViewById<ImageView>(R.id.settingsAccountBackButton)
        val accountName = bottomSheetView.findViewById<TextView>(R.id.settingsAccountProfileNameText)
        val accountNameChangeButton = bottomSheetView.findViewById<ImageView>(R.id.settingsAccountChangeProfileNameButton)
        val accountPasswordChangeButton = bottomSheetView.findViewById<RelativeLayout>(R.id.changePasswordSettings)
        val accountDeleteButton = bottomSheetView.findViewById<RelativeLayout>(R.id.deleteAccountSettings)
        val accountLogoutButton = bottomSheetView.findViewById<RelativeLayout>(R.id.logoutAccountSettings)

        binding.accountUser = user



        accountBackButton.setOnClickListener {
            this.accountBottomSheet?.dismiss()
        }

        accountNameChangeButton.setOnClickListener {
            nameChanger.getChangeNameDialog(user) { updatedName ->
                accountName.text = updatedName
                changeSettingsProfileName(updatedName)
            }
        }

        accountPasswordChangeButton.setOnClickListener {
            resetPassword.getResetSheet(user.userEmail)
        }

        accountDeleteButton.setOnClickListener {
            confirmation.getConfirmationDialog(
                context.getString(R.string.delete_account_message),
            ){
                settingsViewModel.deleteAllNotes()
                settingsViewModel.deleteAllArchive{
                    settingsViewModel.deleteCloud()
                    settingsViewModel.deleteAccount {
                        accountBottomSheet?.dismiss()
                        getAuthActivityForLogout()
                    }
                }
            }
        }

        accountLogoutButton.setOnClickListener {
            confirmation.getConfirmationDialog(
                context.getString(R.string.logout_message),
                ){
                settingsViewModel.logOut {
                    accountBottomSheet?.dismiss()
                    getAuthActivityForLogout()
                }
            }
        }

        accountBottomSheet?.setContentView(bottomSheetView)
        accountBottomSheet?.show()
    }


    private fun getAuthActivityForLogout(){

        val intentToAuthActivity = Intent(activity, AuthActivity::class.java)
        activity.startActivity(intentToAuthActivity)
        activity.finish()

    }

}