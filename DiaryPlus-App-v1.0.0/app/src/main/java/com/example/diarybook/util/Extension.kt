package com.example.diarybook.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.diarybook.R
import com.example.diarybook.model.Diary
import com.google.android.material.textfield.TextInputLayout

import java.util.*

object Constant{
    const val NOTE_DATA = "isNewNote"
    const val ARCHIVE_KEY = "isFromArchive"
    const val CALENDAR_KEY = "isFromCalendar"
    const val EMAIL_DATA = "keepEmail"
    const val WEB_CLIENT_ID = "129189507752-ek24rt2ph5tkgs6g9u4l1dc0e0f6uvo5.apps.googleusercontent.com"
    const val DIARY_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    const val FULL_PHOTO_DATA = "fullPhoto"
    const val LANGUAGE_DATA = "languageData"
    const val CAMERA_PHOTO = "imageUri"
    const val FOLDER_PHOTO_CONTROL = "c"
    const val whiteHexCode = "#FFFFFFFF"
    const val blackHexCode = "#FF000000"
    const val SELECTED_CALENDAR_DATE_EN = "selectedCalendarViewDataEn"
    const val SELECTED_CALENDAR_DATE_TR = "selectedCalendarViewDateTr"
    const val languageCodeTr = "tr"
    const val languageCodeEn = "en"
    const val BACKGROUND_DATA = "fromBackgroundOptions"
    const val monthFormat = "MMMM"
    const val dateFormat = "dd MMMM EEEE"
    const val githubAccount = "https://github.com/mustafakamber"
    const val gmailAccount = "mustkmber@gmail.com"
    const val FILE_PROVIDER_PACKAGE = "com.example.diarybook.fileprovider"
    const val notificationId = 1
    const val nullEmail = ""
    const val channelID = "channel1"
    const val notificationTitle = "titleExtra"
    const val notificationMessage = "messageExtra"
    const val notificationChannelName = "Notification Channel"
    const val notificationDescription = "A Description of the Channel"
    const val baseUrl = "https://raw.githubusercontent.com/"
    const val extUrl = "mustafakamber/CalendarImageDataJSON/main/calendarList.json"
}

fun ImageView.downloadImageFromUrl(url : String?,progressDrawable: CircularProgressDrawable){

    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.base_default_profile_image)

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(this)

}

fun placeHolderProgressBar(context : Context) : CircularProgressDrawable{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius =40f
        start()
    }
}

fun String.setAppLanguage(context: Context){
    val locale = Locale(this)
    Locale.setDefault(locale)
    val config = Configuration()
    config.locale = locale
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

@BindingAdapter("android:setTitleTextTruncate")
fun setTitleTextTruncate(
    textView: TextView,
    fullText : String,
){
    val maxLength = 25
    val truncatedText = if (fullText.length > maxLength) {
        "${fullText.substring(0, maxLength)}..."
    } else {
        fullText
    }
    textView.text = truncatedText
}

@BindingAdapter("android:setContentTextTruncate")
fun setContentTextTruncate(
    textView: TextView,
    fullText : String,
){
    val maxLength = 70
    val truncatedText = if (fullText.length > maxLength) {
        "${fullText.substring(0, maxLength)}..."
    } else {
        fullText
    }
    textView.text = truncatedText
}

@BindingAdapter("android:setTextColor")
fun setTextViewColor(textView : TextView, colorCode : String){
    textView.setTextColor(Color.parseColor(colorCode))
}

@BindingAdapter("android:downloadImage")
fun downloadImageFromUrl(imageView: ImageView,url : String?){

    val options = RequestOptions()
        .placeholder(placeHolderProgressBar(imageView.context))
        .error(R.drawable.base_default_profile_image)

    Glide.with(imageView.context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(imageView)

}

@BindingAdapter("android:setBackgroundColor")
fun setLayoutColor(layout : ConstraintLayout , colorCode : String){
    layout.setBackgroundColor(Color.parseColor(colorCode))
}

@BindingAdapter("android:textPhotoPiece")
fun setPhotoPiece(textView: TextView , photoList : MutableList<Uri>){
    val photoPiece = photoList.size
    if (photoPiece > 0){
        textView.text = " $photoPiece"
    }else{
        textView.visibility = View.GONE
    }
}

@BindingAdapter("android:setDate")
fun setDateLanguage(textView: TextView ,date : Diary){
    val sharedPreferences = SharedPreferences(textView.context)

    val LANGUAGE_CODE = "languageData"
    val CALENDAR_KEY = "isFromCalendar"
    val languageCodeTr = "tr"

    val languageCode = sharedPreferences.getStringData(LANGUAGE_CODE)
    val fromCalendar = sharedPreferences.getBooleanData(CALENDAR_KEY)



    if (fromCalendar){
        textView.visibility = View.GONE
    }else{
        textView.visibility = View.VISIBLE
    }


    if (languageCode== languageCodeTr) {
        textView.text = date.diaryDateTr
    } else {
        textView.text = date.diaryDateEn
    }

}