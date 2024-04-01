package com.example.diarybook.view.sheet

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.example.diarybook.R
import com.example.diarybook.viewmodel.PasswordViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

    class PasswordSheet(private val context : Context,private val passwordViewModel: PasswordViewModel) {

    private lateinit var passwordBottomSheet : BottomSheetDialog


     fun getResetSheet(email: String) {

        passwordBottomSheet = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)

        val bottomSheetView = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_reset_password,
            null
        )

        val emailText = bottomSheetView.findViewById<TextInputEditText>(R.id.resetEmailEdittext)
        val sendButton = bottomSheetView.findViewById<Button>(R.id.resetSendButton)
        val backButton = bottomSheetView.findViewById<TextView>(R.id.resetBackButton)
        val infoText = bottomSheetView.findViewById<TextView>(R.id.resetInfoText)


         backButton.visibility = View.GONE
         infoText.visibility = View.GONE

         emailText.setText(email)

        sendButton.setOnClickListener {
            infoText.visibility = View.VISIBLE

            if (emailText.text.toString().trim() == "") {
                infoText.text = context.getString(R.string.enter_email_error_message)
            } else {

                passwordViewModel.resetPassword(emailText.text.toString().trim(),{

                    sendButtonClicked(infoText,context.getString(R.string.reset_email_info),emailText,sendButton,backButton)

                },{

                    sendButtonClicked(infoText,context.getString(R.string.enter_email_correct_message),emailText,sendButton,backButton)

                })
            }
        }

        passwordBottomSheet?.setContentView(bottomSheetView)
        passwordBottomSheet?.show()
    }

    private fun sendButtonClicked(
        infoText : TextView,
        infoMessage : String,
        emailText : TextInputEditText,
        sendButton : Button,
        backButton : TextView
    ){

        infoText.text = infoMessage

        sendButton.visibility = View.GONE
        backButton.visibility = View.VISIBLE

        emailText.addTextChangedListener {
            sendButton.visibility = View.VISIBLE
            backButton.visibility = View.GONE
        }


        backButton.setOnClickListener {
            passwordBottomSheet?.dismiss()
            return@setOnClickListener
        }

    }
}