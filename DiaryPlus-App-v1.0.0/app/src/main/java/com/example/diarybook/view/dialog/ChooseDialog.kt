package com.example.diarybook.view.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.diarybook.R

class ChooseDialog(private val context: Context) {
    private var alertDialogChoose: AlertDialog? = null

    fun getChooseDialog(onCamera: () -> Unit, onGallery: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout =
            inflater.inflate(R.layout.alert_dialog_choose_camera_gallery, null)

        val cameraButton = dialogLayout.findViewById<RelativeLayout>(R.id.choose_camera_button)
        val galleryButton = dialogLayout.findViewById<RelativeLayout>(R.id.choose_gallery_button)
        val cancelButton = dialogLayout.findViewById<TextView>(R.id.choose_cancel_button)

        cameraButton.setOnClickListener {
            alertDialogChoose?.dismiss()
            onCamera()
        }

        galleryButton.setOnClickListener {
            alertDialogChoose?.dismiss()
            onGallery()
        }

        cancelButton.setOnClickListener {
            alertDialogChoose?.dismiss()
        }

        builder.setView(dialogLayout)
        alertDialogChoose = builder.create()
        alertDialogChoose?.show()
    }
}