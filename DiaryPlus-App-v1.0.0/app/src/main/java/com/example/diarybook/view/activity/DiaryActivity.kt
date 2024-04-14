package com.example.diarybook.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.R
import com.example.diarybook.adapter.DiaryPhotoAdapter
import com.example.diarybook.constant.Constant.BACKGROUND_DATA
import com.example.diarybook.constant.Constant.CAMERA_PHOTO
import com.example.diarybook.constant.Constant.DIARY_AD_ID
import com.example.diarybook.constant.Constant.FOLDER_PHOTO_CONTROL
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.NULL_STRING
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_EN
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_TR
import com.example.diarybook.constant.Constant.blackHexCode
import com.example.diarybook.constant.Constant.darkBlueHexCode
import com.example.diarybook.constant.Constant.languageCodeTr
import com.example.diarybook.constant.Constant.whiteHexCode
import com.example.diarybook.databinding.ActivityDiaryBinding
import com.example.diarybook.model.Diary
import com.example.diarybook.util.checkAppTheme
import com.example.diarybook.util.setAppLanguage
import com.example.diarybook.view.dialog.ChooseDialog
import com.example.diarybook.view.dialog.ConfirmationDialog
import com.example.diarybook.view.dialog.DatePickerDialog
import com.example.diarybook.view.sheet.ColorsSheet
import com.example.diarybook.viewmodel.DiaryViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import java.time.LocalTime
import java.util.UUID


class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryBinding
    private lateinit var viewModel: DiaryViewModel

    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val photoList: MutableList<Uri> = mutableListOf()
    private val deletePhotoList: MutableList<Uri> = mutableListOf()
    private val addPhotoList: MutableList<Uri> = mutableListOf()

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
        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this@DiaryActivity)[DiaryViewModel::class.java]
        datePicker = DatePickerDialog(this@DiaryActivity)
        colorsPicker = ColorsSheet(this@DiaryActivity, viewModel)
        chooser = ChooseDialog(this@DiaryActivity)
        confirmation = ConfirmationDialog(this@DiaryActivity)

        registerLauncher()
        observeLiveData()
        setupCommonScreen()
    }


    override fun onPause() {
        super.onPause()

        viewModel.deleteData(CAMERA_PHOTO)
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getPhotoFromCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        addPhotoList.clear()
        deletePhotoList.clear()
        photoList.clear()
        viewModel.deleteData(BACKGROUND_DATA)
        viewModel.deleteData(CAMERA_PHOTO)
    }

    private fun setupCommonScreen() = with(binding) {

        getBottomPhotoView(photoList)

        setupAd()

        noteTitleEditText.addTextChangedListener {
            viewModel.setVisibleTitleEndIcon()
        }

        noteContentEditText.addTextChangedListener {
            viewModel.setVisibleContentEndIcon()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmation.getConfirmationDialog(
                    getString(R.string.note_cancel_message)
                ) {
                    navigateToHomeScreen()
                }
            }
        }
        this@DiaryActivity.onBackPressedDispatcher.addCallback(callback)

        noteBackButton.setOnClickListener {
            confirmation.getConfirmationDialog(
                getString(R.string.note_cancel_message)
            ) {
                navigateToHomeScreen()
            }
        }

        val languageCode = viewModel.getStringData(LANGUAGE_DATA)
        languageCode!!.setAppLanguage(this@DiaryActivity)

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
                    navigateCameraScreen()
                },
                onGallery = {
                    getPhotoFromGallery()
                }
            )
        }
        noteBackgroundColorButton.setOnClickListener {
            updateOptionsData(viewModel, true)
            colorsPicker.getColorsSheet(
                selectedTextColor,
                selectedBackgroundColor
            ) { newColor ->
                changeBackgroundColor(newColor)
            }
        }
        noteTextColorButton.setOnClickListener {
            updateOptionsData(viewModel, false)
            colorsPicker.getColorsSheet(
                selectedTextColor,
                selectedBackgroundColor
            ) { newColor ->
                changeTextColor(newColor)
            }
        }
    }

    private fun setupAd(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this@DiaryActivity, DIARY_AD_ID,
            adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.let {
                        it.toString()?.let { Log.d(TAG, it) }
                    }
                    diaryAdView = null
                }
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    diaryAdView = interstitialAd
                }
            })
    }

    private fun setupAddDiaryScreen() = with(binding) {
        noteSaveButton.visibility = View.VISIBLE
        checkAppTheme(this@DiaryActivity,{
            selectedTextColor = blackHexCode
            selectedBackgroundColor = whiteHexCode
        },{
            selectedTextColor = whiteHexCode
            selectedBackgroundColor = darkBlueHexCode
        })
        noteActivityConstraintLayout.setBackgroundColor(
            Color.parseColor(selectedBackgroundColor)
        )
        noteTitleEditText.setTextColor(Color.parseColor(selectedTextColor))
        noteContentEditText.setTextColor(Color.parseColor(selectedTextColor))
        val dateFromCalendarFragmentEn = intent.getStringExtra(SELECTED_CALENDAR_DATE_EN)
        if (dateFromCalendarFragmentEn != null) {
            val dateFromCalendarFragmentTr =
                intent.getStringExtra(SELECTED_CALENDAR_DATE_TR)
            if (viewModel.getStringData(LANGUAGE_DATA) == languageCodeTr) {
                noteCalendarText.setText(dateFromCalendarFragmentTr).toString()
            } else {
                noteCalendarText.setText(dateFromCalendarFragmentEn).toString()
            }
            diaryDateEn = dateFromCalendarFragmentEn.toString()
            diaryDateTr = dateFromCalendarFragmentTr.toString()
        } else {
            if (viewModel.getStringData(LANGUAGE_DATA) == languageCodeTr) {
                noteCalendarText.setText(datePicker.currentDateTr()).toString()
            } else {
                noteCalendarText.setText(datePicker.currentDateEn()).toString()
            }
            diaryDateEn = datePicker.currentDateEn()
            diaryDateTr = datePicker.currentDateTr()
        }
        noteActivityTitleText.text = getString(R.string.note_activity_title_new)
        noteTitleEditText.setText(NULL_STRING)
        noteContentEditText.setText(NULL_STRING)
        noteSaveButton.setOnClickListener {
            noteSaveButton.isClickable = false
            saveOperations()
        }
    }



    private fun setupOldDiaryScreen(diary: Diary) = with(binding) {
        noteMenuButton.visibility = View.VISIBLE
        selectedBackgroundColor = diary.diaryBackgroundColor
        selectedTextColor = diary.diaryTextColor
        noteActivityConstraintLayout.setBackgroundColor(
            Color.parseColor(
                selectedBackgroundColor
            )
        )
        noteTitleEditText.setTextColor(Color.parseColor(selectedTextColor))
        noteContentEditText.setTextColor(Color.parseColor(selectedTextColor))
        if (viewModel.getStringData(LANGUAGE_DATA) == languageCodeTr) {
            noteCalendarText.text = diary.diaryDateTr
        } else {
            noteCalendarText.text = diary.diaryDateEn
        }
        diaryDateEn = diary.diaryDateEn
        diaryDateTr = diary.diaryDateTr
        noteActivityTitleText.text = getString(R.string.note_activity_title_old)
        noteTitleEditText.setText(diary.diaryTitle)
        noteContentEditText.setText(diary.diaryContent)
        diary.diaryPhoto?.let {
            photoList.addAll(diary.diaryPhoto)
        }
        getBottomPhotoView(photoList)
        noteMenuButton.setOnClickListener {
            showNoteMenu({
                //noteMenuButton.isClickable = false
                viewModel.deleteNote(diary.diaryId!!)
            }, {
                //noteMenuButton.isClickable = false
                updateOperations(diary.diaryId!!)
            })
        }
    }


    private fun observeLiveData() = with(binding) {
        viewModel.toastMessage.observe(this@DiaryActivity) { error ->
            error?.let {
                Toast.makeText(this@DiaryActivity, error, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.snackbarMessage.observe(this@DiaryActivity) { message ->
            message?.let {
                Snackbar.make(binding.root, getString(message), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
        viewModel.permissionMessage.observe(this@DiaryActivity) { permissionMessage ->
            permissionMessage?.let {
                permissionLauncherGallery.launch(permissionMessage)
            }
        }
        viewModel.gallerySuccess.observe(this@DiaryActivity) { gallerySuccess ->
            gallerySuccess?.let {
                if (gallerySuccess) {
                    activityResultLauncher.launch(
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    )
                }
            }
        }
        viewModel.actionSnackbarMessage.observe(this@DiaryActivity) { actionSnackbar ->
            val snackbarText = actionSnackbar.snackbarText
            val allowButtonText = actionSnackbar.allowButtonText
            val permissionMessage = actionSnackbar.permissionMessage
            Snackbar.make(binding.root, getString(snackbarText), Snackbar.LENGTH_INDEFINITE)
                .setAction(
                    getString(allowButtonText),
                    View.OnClickListener {
                        permissionLauncherGallery.launch(permissionMessage)
                    }).show()
        }
        viewModel.titleErrorText.observe(this@DiaryActivity) { titleError ->
            titleError?.let {
                noteTitleEditText.error = getString(titleError)
            }
        }
        viewModel.titleEndIconVisible.observe(this@DiaryActivity) { titleVisibleBoolean ->
            titleVisibleBoolean?.let {
                newNoteTitleLayout.isEndIconVisible = titleVisibleBoolean
            }
        }
        viewModel.contentErrorText.observe(this@DiaryActivity) { contentError ->
            contentError?.let {
                noteContentEditText.error = getString(contentError)
            }
        }
        viewModel.contentEndIconVisible.observe(this@DiaryActivity)
        { contentVisibleBoolean ->
            contentVisibleBoolean?.let {
                newNoteContentLayout.isEndIconVisible = contentVisibleBoolean
            }
        }
        viewModel.exitSuccess.observe(this@DiaryActivity) { success ->
            success?.let {
                if (success) {
                    navigateToHomeScreen()
                }
            }
        }
        viewModel.newDiaryCheck.observe(this@DiaryActivity) { diaryCheck ->
            diaryCheck?.let {
                if (diaryCheck.isNewDiary && diaryCheck.diary == null){
                    setupAddDiaryScreen()
                }else if (!diaryCheck.isNewDiary && diaryCheck.diary != null){
                    setupOldDiaryScreen(diaryCheck.diary)
                }
            }
        }
        viewModel.photoFromGalleryCamera.observe(this@DiaryActivity) { photo ->
            photo?.let {
                photoList.add(photo)
                addPhotoList.add(photo)
                getBottomPhotoView(photoList)
            }
        }
    }

    private fun getBottomPhotoView(photoList: MutableList<Uri>) {
        if (photoList.size == 0) {
            binding.noteRecyclerView.visibility = View.GONE
        } else {
            binding.noteRecyclerView.visibility = View.VISIBLE
        }
        val notePhotoAdapter = DiaryPhotoAdapter(photoList) { notePhoto ->
            if (notePhoto.scheme?.startsWith(FOLDER_PHOTO_CONTROL, ignoreCase = true) == true) {
                addPhotoList.remove(notePhoto)
            } else {
                deletePhotoList.add(notePhoto)
            }
            photoList.remove(notePhoto)
            getBottomPhotoView(photoList)
        }
        binding.noteRecyclerView.adapter = notePhotoAdapter
    }

    private fun navigateCameraScreen() {
        val intentToCameraActivity =
            Intent(this@DiaryActivity, CameraActivity::class.java)
        startActivity(intentToCameraActivity)
    }

    private fun changeBackgroundColor(newColor: String) = with(binding) {
        Snackbar.make(binding.root, R.string.background_color_changed, Snackbar.LENGTH_SHORT).show()
        selectedBackgroundColor = newColor
        noteActivityConstraintLayout.setBackgroundColor(Color.parseColor(selectedBackgroundColor))
    }

    private fun changeTextColor(newColor: String) = with(binding) {
        Snackbar.make(binding.root, R.string.text_color_changed, Snackbar.LENGTH_SHORT)
            .show()
        selectedTextColor = newColor
        noteTitleEditText.setTextColor(Color.parseColor(selectedTextColor))
        noteContentEditText.setTextColor(Color.parseColor(selectedTextColor))
    }

    private fun updateOptionsData(viewModel: DiaryViewModel, newValue: Boolean) {
        val previousOptionsBoolean = viewModel.getBooleanData(BACKGROUND_DATA)
        if (previousOptionsBoolean != null) {
            viewModel.deleteData(BACKGROUND_DATA)
        }
        viewModel.saveBooleanData(BACKGROUND_DATA, newValue)
    }

    private fun updateOperations(noteId: String) = with(binding) {
        val noteTitle = noteTitleEditText.text.toString().trim()
        val noteContent = noteContentEditText.text.toString().trim()
        val noteBackgroundColor = selectedBackgroundColor
        val noteTextColor = selectedTextColor
        val noteDateTr = diaryDateTr
        val noteDateEn = diaryDateEn
        val notePhoto = addPhotoList
        val currentClock = LocalTime.now()
        val noteTime = String.format("%02d:%02d", currentClock.hour, currentClock.minute)
        viewModel.inputControl(
            noteId,
            noteTitle,
            noteContent,
            notePhoto,
            deletePhotoList,
            noteTextColor,
            noteBackgroundColor,
            noteDateTr,
            noteDateEn,
            noteTime
        )
    }

    private fun saveOperations() = with(binding) {
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
        viewModel.inputControl(
            noteId,
            noteTitle,
            noteContent,
            notePhoto,
            null,
            noteTextColor,
            noteBackgroundColor,
            noteDateTr,
            noteDateEn,
            noteTime
        )
    }

    private fun navigateToHomeScreen() {
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
        val popupMenu = PopupMenu(this, binding.noteMenuButton)
        popupMenu.menuInflater.inflate(R.menu.menu_note, popupMenu.menu)
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
        viewModel.checkSelfPermissionAccessToGallery(this@DiaryActivity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                viewModel.checkSelfPermissionAccessToCamera(this)
            }
        }
    }

    private fun registerLauncher() {
        viewModel.checkSelfPermissionAccessToCamera(this@DiaryActivity)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { galleryResult ->
            viewModel.getPhotoFromGallery(galleryResult)
        }
        permissionLauncherGallery = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { galleryPermissionResult ->
            viewModel.handleGalleryPermissionResult(this@DiaryActivity, galleryPermissionResult)
        }
    }
}



