package com.example.diarybook.view.activity

import android.Manifest
import android.content.ContentValues.TAG
import com.example.diarybook.view.dialog.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import com.example.diarybook.R
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.adapter.DiaryPhotoAdapter
import com.example.diarybook.databinding.ActivityDiaryBinding
import com.example.diarybook.util.*
import com.example.diarybook.constant.Constant.BACKGROUND_DATA
import com.example.diarybook.constant.Constant.CAMERA_PHOTO
import com.example.diarybook.constant.Constant.DIARY_AD_ID
import com.example.diarybook.constant.Constant.FOLDER_PHOTO_CONTROL
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_EN
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_TR
import com.example.diarybook.constant.Constant.blackHexCode
import com.example.diarybook.constant.Constant.languageCodeTr
import com.example.diarybook.constant.Constant.whiteHexCode
import com.example.diarybook.view.dialog.ChooseDialog
import com.example.diarybook.view.dialog.ConfirmationDialog
import com.example.diarybook.view.sheet.ColorsSheet
import com.example.diarybook.viewmodel.DiaryViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import java.time.LocalTime
import java.util.*


class DiaryActivity : AppCompatActivity() {

    private lateinit var diaryActivityBinding: ActivityDiaryBinding
    private lateinit var diaryViewModel: DiaryViewModel

    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val photoList: MutableList<Uri> = mutableListOf()
    private val deletePhotoList: MutableList<Uri> = mutableListOf()
    private val addPhotoList: MutableList<Uri> = mutableListOf()

    private lateinit var notePhotoView: RecyclerView

    private val handler = Handler()
    private var diaryDateEn: String = ""
    private var diaryDateTr: String = ""
    private var selectedTextColor: String = ""
    private var selectedBackgroundColor: String = ""

    private var diaryAdView: InterstitialAd? = null

    private lateinit var datePicker: DatePickerDialog
    private lateinit var colorsPicker: ColorsSheet
    private lateinit var chooser: ChooseDialog
    private lateinit var confirmation: ConfirmationDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diaryActivityBinding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(diaryActivityBinding.root)
        with(diaryActivityBinding) {

            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(this@DiaryActivity, DIARY_AD_ID,
                adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adError?.toString()?.let { Log.d(TAG, it) }
                        diaryAdView = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        diaryAdView = interstitialAd
                    }
                })

            diaryViewModel = ViewModelProvider(this@DiaryActivity)[DiaryViewModel::class.java]

            val languageCode = diaryViewModel.getStringData(LANGUAGE_DATA)

            datePicker = DatePickerDialog(this@DiaryActivity)
            colorsPicker = ColorsSheet(this@DiaryActivity, diaryViewModel)
            chooser = ChooseDialog(this@DiaryActivity)
            confirmation = ConfirmationDialog(this@DiaryActivity)

            languageCode!!.setAppLanguage(this@DiaryActivity)

            noteSaveButton.visibility = View.GONE
            noteMenuButton.visibility = View.GONE

            registerLauncher()

            notePhotoView = noteRecyclerView

            notePhotoView.layoutManager = LinearLayoutManager(
                this@DiaryActivity,
                RecyclerView.HORIZONTAL,
                false
            )

            diaryViewModel.newDiaryCheck()

            observeLiveData(datePicker)


            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmation.getConfirmationDialog(
                        getString(R.string.note_cancel_message)
                    ) {
                        backToBaseActivity()
                    }
                }
            }

            this@DiaryActivity.onBackPressedDispatcher.addCallback(callback)

            noteBackButton.setOnClickListener {

                confirmation.getConfirmationDialog(
                    getString(R.string.note_cancel_message)
                ) {
                    backToBaseActivity()
                }

            }

            noteCalendarButton.setOnClickListener {

                datePicker.getDatePickerDialog { selectedDateEn, selectedDateTr ->
                    diaryDateEn = selectedDateEn
                    diaryDateTr = selectedDateTr
                    if (languageCode == languageCodeTr) {
                        noteCalendarText.text = selectedDateTr
                    } else {
                        noteCalendarText.text = selectedDateEn
                    }
                }

            }

            noteCameraButton.setOnClickListener {

                chooser.getChooseDialog(
                    onCamera = {
                        getCameraActivity()
                    },
                    onGallery = {
                        getPhotoFromGallery()
                    }
                )

            }
            noteBackgroundColorButton.setOnClickListener {
                updateOptionsData(diaryViewModel, true)
                colorsPicker.getColorsSheet(
                    selectedTextColor,
                    selectedBackgroundColor
                ) { newColor ->
                    changeBackgroundColor(newColor)
                }
            }
            noteTextColorButton.setOnClickListener {
                updateOptionsData(diaryViewModel, false)
                colorsPicker.getColorsSheet(
                    selectedTextColor,
                    selectedBackgroundColor
                ) { newColor ->
                    changeTextColor(newColor)
                }
            }

        }
    }


    override fun onPause() {
        super.onPause()

        diaryViewModel.deleteData(CAMERA_PHOTO)
    }

    override fun onRestart() {
        super.onRestart()
        getPhotoFromCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        addPhotoList.clear()
        deletePhotoList.clear()
        photoList.clear()
        diaryViewModel.deleteData(BACKGROUND_DATA)
        diaryViewModel.deleteData(CAMERA_PHOTO)
    }


    private fun observeLiveData(datePicker: DatePickerDialog) = with(diaryActivityBinding) {

        diaryViewModel.diaryToastMessage.observe(this@DiaryActivity) { error ->
            error?.let {
                Toast.makeText(this@DiaryActivity, error, Toast.LENGTH_SHORT).show()
            }
        }

        diaryViewModel.diarySnackbarMessage.observe(this@DiaryActivity) { message ->
            message?.let {
                Snackbar.make(diaryActivityBinding.root, getString(message), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        diaryViewModel.createNewDiary.observe(this@DiaryActivity) { newDiary ->
            if (newDiary) {

                noteSaveButton.visibility = View.VISIBLE

                selectedBackgroundColor = whiteHexCode
                selectedTextColor = blackHexCode

                noteActivityConstraintLayout.setBackgroundColor(
                    Color.parseColor(selectedBackgroundColor)
                )
                noteTitleEditText.setTextColor(Color.parseColor(selectedTextColor))
                noteContentEditText.setTextColor(Color.parseColor(selectedTextColor))


                val dateFromCalendarFragmentEn = intent.getStringExtra(SELECTED_CALENDAR_DATE_EN)

                if (dateFromCalendarFragmentEn != null) {

                    val dateFromCalendarFragmentTr =
                        intent.getStringExtra(SELECTED_CALENDAR_DATE_TR)
                    if (diaryViewModel.getStringData(LANGUAGE_DATA) == languageCodeTr) {
                        noteCalendarText.setText(dateFromCalendarFragmentTr).toString()
                    } else {
                        noteCalendarText.setText(dateFromCalendarFragmentEn).toString()
                    }

                    diaryDateEn = dateFromCalendarFragmentEn.toString()
                    diaryDateTr = dateFromCalendarFragmentTr.toString()

                } else {

                    if (diaryViewModel.getStringData(LANGUAGE_DATA) == languageCodeTr) {
                        noteCalendarText.setText(datePicker.currentDateTr()).toString()
                    } else {
                        noteCalendarText.setText(datePicker.currentDateEn()).toString()
                    }

                    diaryDateEn = datePicker.currentDateEn()
                    diaryDateTr = datePicker.currentDateTr()

                }

                noteActivityTitleText.text = getString(R.string.note_activity_title_new)

                noteTitleEditText.setText("")
                noteContentEditText.setText("")

                noteSaveButton.setOnClickListener {
                    noteSaveButton.isClickable = false
                    saveOperations()
                }
            }
        }


        diaryViewModel.showOldDiary.observe(this@DiaryActivity) { diary ->
            diary?.let { diary ->

                noteMenuButton.visibility = View.VISIBLE

                selectedBackgroundColor = diary.diaryBackgroundColor!!
                selectedTextColor = diary.diaryTextColor!!

                noteActivityConstraintLayout.setBackgroundColor(
                    Color.parseColor(
                        selectedBackgroundColor
                    )
                )
                noteTitleEditText.setTextColor(Color.parseColor(selectedTextColor))
                noteContentEditText.setTextColor(Color.parseColor(selectedTextColor))

                if (diaryViewModel.getStringData(LANGUAGE_DATA) == languageCodeTr) {
                    noteCalendarText.text = diary.diaryDateTr
                } else {
                    noteCalendarText.text = diary.diaryDateEn
                }

                diaryDateEn = diary.diaryDateEn!!
                diaryDateTr = diary.diaryDateTr!!

                noteActivityTitleText.text = getString(R.string.note_activity_title_old)

                noteTitleEditText.setText(diary.diaryTitle)
                noteContentEditText.setText(diary.diaryContent)

                photoList.addAll(diary.diaryPhoto!!)

                getBottomPhotoView(photoList)

                noteMenuButton.setOnClickListener {
                    showNoteMenu({
                        noteMenuButton.isClickable = false
                        deleteDiary(diary.diaryId!!)
                    }, {
                        noteMenuButton.isClickable = false
                        updateOperations(diary.diaryId!!)
                    })
                }
            }
        }

    }

    private fun getCameraActivity() {
        val intentToCameraActivity =
            Intent(this@DiaryActivity, CameraActivity::class.java)
        startActivity(intentToCameraActivity)
    }

    private fun changeBackgroundColor(newColor: String) = with(diaryActivityBinding) {

        Snackbar.make(
            diaryActivityBinding.root,
            R.string.background_color_changed,
            Snackbar.LENGTH_SHORT
        ).show()

        selectedBackgroundColor = newColor
        noteActivityConstraintLayout.setBackgroundColor(Color.parseColor(selectedBackgroundColor))
    }

    private fun changeTextColor(newColor: String) = with(diaryActivityBinding) {

        Snackbar.make(diaryActivityBinding.root, R.string.text_color_changed, Snackbar.LENGTH_SHORT)
            .show()

        selectedTextColor = newColor
        noteTitleEditText.setTextColor(Color.parseColor(selectedTextColor))
        noteContentEditText.setTextColor(Color.parseColor(selectedTextColor))
    }


    private fun getBottomPhotoView(photoList: MutableList<Uri>) {

        val notePhotoAdapter = DiaryPhotoAdapter(photoList) { notePhoto ->

            if (notePhoto.scheme?.startsWith(FOLDER_PHOTO_CONTROL, ignoreCase = true) == true) {
                addPhotoList.remove(notePhoto)
            } else {
                deletePhotoList.add(notePhoto)
            }

            photoList.remove(notePhoto)
            getBottomPhotoView(photoList)
        }
        notePhotoView.adapter = notePhotoAdapter
    }

    private fun updateOptionsData(viewModel: DiaryViewModel, newValue: Boolean) {
        val previousOptionsBoolean = viewModel.getBooleanData(BACKGROUND_DATA)

        if (previousOptionsBoolean != null) {
            viewModel.deleteData(BACKGROUND_DATA)
        }

        viewModel.saveBooleanData(BACKGROUND_DATA, newValue)
    }

    private fun deleteDiary(noteId: String) {

        diaryViewModel.deleteNote(noteId) {
            handler.postDelayed({
                backToBaseActivity()
            }, 750)
        }

    }

    private fun updateOperations(noteId: String) = with(diaryActivityBinding) {

        val noteTitle = noteTitleEditText.text.toString().trim()
        val noteContent = noteContentEditText.text.toString().trim()
        val noteBackgroundColor = selectedBackgroundColor
        val noteTextColor = selectedTextColor
        val noteDateTr = diaryDateTr
        val noteDateEn = diaryDateEn
        val notePhoto = addPhotoList
        val currentClock = LocalTime.now()
        val noteTime = String.format("%02d:%02d", currentClock.hour, currentClock.minute)


        nullInputControl(noteTitle, noteContent) {
            diaryViewModel.updateDiary(
                deletePhotoList, noteId, noteTitle,
                noteContent, notePhoto, noteDateEn, noteDateTr,
                noteBackgroundColor, noteTextColor, noteTime
            ) {
                handler.postDelayed({
                    backToBaseActivity()
                }, 750)
            }
        }

    }


    private fun saveOperations() = with(diaryActivityBinding) {

        val noteId = UUID.randomUUID().toString()
        val noteTitle = noteTitleEditText.text.toString().trim()
        val noteContent = noteContentEditText.text.toString().trim()
        val noteBackgroundColor = selectedBackgroundColor
        val noteTextColor = selectedTextColor
        val noteDateTr = diaryDateTr
        val noteDateEn = diaryDateEn
        val notePhoto = addPhotoList
        val currentClock = LocalTime.now()
        val noteTime = String.format("%02d:%02d", currentClock.hour, currentClock.minute)

        nullInputControl(noteTitle, noteContent) {
            diaryViewModel.saveDiary(
                noteId, noteTitle, noteContent,
                notePhoto, noteDateEn, noteDateTr,
                noteBackgroundColor, noteTextColor, noteTime
            ) {
                handler.postDelayed({
                    backToBaseActivity()
                }, 750)
            }
        }
    }

    private fun nullInputControl(noteTitle: String, noteContent: String, onSuccess: () -> Unit) =
        with(diaryActivityBinding) {
            if (TextUtils.isEmpty(noteTitle) || TextUtils.isEmpty(noteContent)) {
                if (TextUtils.isEmpty(noteTitle)) {
                    newNoteTitleLayout.isEndIconVisible = false
                    noteTitleEditText.error = getString(R.string.note_title_error_message)
                    noteTitleEditText.addTextChangedListener {
                        newNoteTitleLayout.isEndIconVisible = true
                    }
                }
                if (TextUtils.isEmpty(noteContent)) {
                    newNoteContentLayout.isEndIconVisible = false
                    noteContentEditText.error = getString(R.string.note_content_error_message)
                    noteContentEditText.addTextChangedListener {
                        newNoteContentLayout.isEndIconVisible = true
                    }
                }
                noteSaveButton.isClickable = true
                noteMenuButton.isClickable = true
                return@with
            } else {
                onSuccess()
            }
        }

    private fun backToBaseActivity() {

        if (diaryAdView != null) {

            diaryAdView?.show(this@DiaryActivity)

            diaryAdView?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    diaryAdView = null
                    this@DiaryActivity.finish()
                }

            }
        } else {
            this@DiaryActivity.finish()
        }

    }

    private fun showNoteMenu(onDelete: () -> Unit, onUpdate: () -> Unit) {
        val popupMenu = PopupMenu(this, diaryActivityBinding.noteMenuButton)

        popupMenu.menuInflater.inflate(
            R.menu.menu_note,
            popupMenu.menu
        )

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_update -> {
                    onUpdate()
                    true
                }
                R.id.menu_delete -> {
                    onDelete()
                    true
                }
                else -> false
            }

        }

        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass
                .getDeclaredMethod(
                    "setForceShowIcon",
                    Boolean::class.java
                )
                .invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            popupMenu.show()
        }
    }


    private fun getPhotoFromGallery() {
        diaryViewModel.checkSelfPermissionAccessToGallery(this@DiaryActivity, {
            activityResultLauncher.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }, {
            permissionLauncherGallery
                .launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }, {
            Snackbar.make(
                diaryActivityBinding.root,
                R.string.read_external_storage_permission,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(
                    R.string.allow,
                    View.OnClickListener {
                        permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                diaryViewModel.checkSelfPermissionAccessToCamera(this)
            }
        }
    }


    private fun registerLauncher() {

        diaryViewModel.checkSelfPermissionAccessToCamera(this@DiaryActivity)

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { galleryResult ->
            diaryViewModel.getPhotoFromGallery(
                galleryResult
            ) { selectedGalleryPhoto ->
                if (selectedGalleryPhoto != null) {
                    photoList.add(selectedGalleryPhoto)
                    addPhotoList.add(selectedGalleryPhoto)
                    getBottomPhotoView(photoList)
                }
            }
        }

        permissionLauncherGallery = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { galleryPermissionResult ->
            if (galleryPermissionResult) {
                activityResultLauncher.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            } else {
                diaryViewModel.diaryToastMessage.value =
                    getString(R.string.read_external_storage_permission)
            }
        }

    }

    private fun getPhotoFromCamera() {

        val selectedCameraPhotoString = diaryViewModel.getStringData(CAMERA_PHOTO)
        if (selectedCameraPhotoString != "") {
            val selectedCameraPhoto = selectedCameraPhotoString!!.toUri()
            photoList.add(selectedCameraPhoto)
            addPhotoList.add(selectedCameraPhoto)
            getBottomPhotoView(photoList)
        }

    }


}



