package com.example.diarybook.view.dialog

import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.diarybook.R
import com.example.diarybook.model.User
import com.example.diarybook.viewmodel.SettingsViewModel


class NameDialog(private val activity: Activity, private val context: Context, private val viewModel: SettingsViewModel) {

    private var alertDialogChangeName: AlertDialog? = null

    fun getChangeNameDialog(user: User, onSuccess: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_change_name, null)

        val changeNameEditText = dialogLayout.findViewById<EditText>(R.id.change_name_edittext)
        val changeNameButton = dialogLayout.findViewById<Button>(R.id.change_name_button)

        changeNameButton.setOnClickListener {
            val newName = changeNameEditText.text.toString().trim()

            if (newName.isEmpty()) {
                changeNameEditText.error = context.getString(R.string.enter_name_error_message)
            } else {
                val updatedUser = User(user.userEmail, newName, user.userImage)
                viewModel.addNewUserOrUpdateToDB(updatedUser) {
                    onSuccess(newName)
                }
            }
        }
        builder.setView(dialogLayout)
        alertDialogChangeName = builder.create()
        alertDialogChangeName?.show()
    }


}