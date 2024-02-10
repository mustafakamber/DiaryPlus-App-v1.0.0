package com.example.diarybook.view.dialog

import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.example.diarybook.R


class LanguageDialog(private val context : Context,private val activity : Activity) {


    private var alertDialogLanguage: AlertDialog? = null

    fun getLanguageDialog(onTurkish: () -> Unit, onEnglish: () -> Unit) {

        val builder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_language, null)

        val languageTurkish = dialogLayout.findViewById<RadioButton>(R.id.languageTurkish)
        val languageEnglish = dialogLayout.findViewById<RadioButton>(R.id.languageEnglish)
        val languageRadioGroup = dialogLayout.findViewById<RadioGroup>(R.id.languageGroup)
        val languageSubmitButton = dialogLayout.findViewById<Button>(R.id.languageSubmitButton)

        builder.setView(dialogLayout)
        alertDialogLanguage = builder.create()
        alertDialogLanguage?.show()

        languageRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.languageTurkish) {
                languageEnglish.isChecked = false
            }
            if (checkedId == R.id.languageEnglish) {
                languageTurkish.isChecked = false
            }
        }

        languageSubmitButton.setOnClickListener {
            val selectedRadioButtonId = languageRadioGroup.checkedRadioButtonId
            if (selectedRadioButtonId == R.id.languageTurkish) {
                alertDialogLanguage?.dismiss()
                onTurkish()
            } else if (selectedRadioButtonId == R.id.languageEnglish) {
                alertDialogLanguage?.dismiss()
                onEnglish()
            }
        }

    }
}