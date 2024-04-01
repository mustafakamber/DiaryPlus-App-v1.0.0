package com.example.diarybook.view.sheet

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.diarybook.R
import com.example.diarybook.databinding.BottomSheetDatabaseBinding
import com.example.diarybook.view.dialog.ConfirmationDialog
import com.example.diarybook.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class DatabaseSheet(
    private val context : Context,
    private val settingsViewModel : SettingsViewModel
    ) {

    private lateinit var databaseBottomSheet : BottomSheetDialog

    val confirmation = ConfirmationDialog(context)


    @SuppressLint("MissingInflatedId")
    fun getDatabaseSheet(){

        databaseBottomSheet = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)

        val bottomSheetView = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_database,
            null
        )

        val databaseBackButton = bottomSheetView.findViewById<ImageView>(R.id.settingsDatabaseBackButton)
        val databaseDeleteButton = bottomSheetView.findViewById<RelativeLayout>(R.id.deleteSettings)
        val databaseArchiveButton = bottomSheetView.findViewById<RelativeLayout>(R.id.archiveSettings)
        val databaseUnArchiveButton = bottomSheetView.findViewById<RelativeLayout>(R.id.restoreSettings)
        val databaseBackupButton = bottomSheetView.findViewById<RelativeLayout>(R.id.backupSettings)
        val databaseRestoreButton = bottomSheetView.findViewById<RelativeLayout>(R.id.backloadSettings)
        val databaseClearButton = bottomSheetView.findViewById<RelativeLayout>(R.id.clearSettings)

        databaseBackButton.setOnClickListener {
            this.databaseBottomSheet?.dismiss()
        }

        databaseDeleteButton.setOnClickListener {
            confirmation.getConfirmationDialog(
                context.getString(R.string.delete_note_message)
            ){
                settingsViewModel.deleteAllNotes()
            }
        }

        databaseArchiveButton.setOnClickListener {

            confirmation.getConfirmationDialog(
                context.getString(R.string.archive_note_message)
            ) {
                settingsViewModel.moveAllNoteToArchive()
            }

        }

        databaseUnArchiveButton.setOnClickListener {

            confirmation.getConfirmationDialog(
                context.getString(R.string.note_archive_message)
            ) {
                settingsViewModel.moveAllArchiveToNote()
            }

        }

        databaseBackupButton.setOnClickListener {

            confirmation.getConfirmationDialog(
                context.getString(R.string.note_backup_message)
            ) {
                settingsViewModel.backupAllNotes()
            }

        }

        databaseRestoreButton.setOnClickListener {

            confirmation.getConfirmationDialog(
                context.getString(R.string.note_backload_message)
            ){
                settingsViewModel.backloadAllNotes()
            }

        }

        databaseClearButton.setOnClickListener {
            confirmation.getConfirmationDialog(
                context.getString(R.string.note_clear_message)
            ){
                settingsViewModel.deleteCloud()
            }
        }

        databaseBottomSheet?.setContentView(bottomSheetView)
        databaseBottomSheet?.show()

    }

}