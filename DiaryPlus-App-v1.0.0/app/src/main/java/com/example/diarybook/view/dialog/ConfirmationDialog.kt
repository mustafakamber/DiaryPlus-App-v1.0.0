package com.example.diarybook.view.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.diarybook.R

class ConfirmationDialog(private val context: Context) {

    private var alertDialogConfirmation: AlertDialog? = null

    fun getConfirmationDialog(titleText: String, onBack: () -> Unit) {

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout =
            inflater.inflate(R.layout.alert_dialog_confirmation, null) as ConstraintLayout

        val titleTextView = dialogLayout.findViewById<TextView>(R.id.dialogTitleText)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.dialogPositiveButton)
        val negativeButton = dialogLayout.findViewById<TextView>(R.id.dialogNegativeButton)

        titleTextView.text = titleText
        positiveButton.text = context.getString(R.string.yes)
        negativeButton.text = context.getString(R.string.no)

        builder.setView(dialogLayout)
        alertDialogConfirmation = builder.create()
        alertDialogConfirmation?.show()

        positiveButton.setOnClickListener {
            alertDialogConfirmation?.dismiss()
            onBack()
        }

        negativeButton.setOnClickListener {
            alertDialogConfirmation?.dismiss()
        }
    }
}
