package com.example.diarybook.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.MODE_DATA
import com.example.diarybook.model.Diary

import java.util.*

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

fun String.setAppLanguage(context: Context) {
    val locale = Locale(this)
    Locale.setDefault(locale)
    val config = Configuration()
    config.locale = locale
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

fun checkAppTheme(
    context: Context,
    lightMode: () -> Unit,
    darkMode: () -> Unit
) {
    /*
    //Uygulamanın temasına göre kontrol (Özel)
    val sharedPreferences = SharedPreferences(context)
    val currentNightMode = sharedPreferences.getBooleanData(MODE_DATA)
    if (currentNightMode){
        darkMode()
    }else{
        lightMode()
    }
    */

    //Telefonun temasına göre kontrol (Genel)
    val currentNightMode = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    when (currentNightMode) {
        Configuration.UI_MODE_NIGHT_NO -> {
            lightMode()
        }
        Configuration.UI_MODE_NIGHT_YES -> {
            darkMode()
        }
    }

}

fun setLightTheme(context: Context){
    val sharedPreferences = SharedPreferences(context)
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    sharedPreferences.saveBooleanData(MODE_DATA,false)
}

fun setDarkTheme(context: Context){
    val sharedPreferences = SharedPreferences(context)
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    sharedPreferences.saveBooleanData(MODE_DATA,true)
}


@BindingAdapter("android:setTitleTextTruncate")
fun setTitleTextTruncate(
    textView: TextView,
    fullText: String,
) {
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
fun setTextViewColor(textView: TextView, colorCode: String) {
    textView.setTextColor(Color.parseColor(colorCode))
}

@BindingAdapter("android:setImageColor")
fun setImageViewColor(imageView: ImageView, colorCode: String) {
    imageView.setColorFilter(Color.parseColor(colorCode))
}

@BindingAdapter("android:downloadImage")
fun downloadImageFromUrl(imageView: ImageView, url: String?) {
    val options = RequestOptions()
        .placeholder(placeHolderProgressBar(imageView.context))
        .error(R.drawable.default_profile_image_home)
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
fun setPhotoPieceText(textView: TextView, photoList: MutableList<Uri>) {
    val photoPiece = photoList.size
    if (photoPiece > 0) {
        textView.text = " $photoPiece"
    } else {
        textView.visibility = View.GONE
    }
}

@BindingAdapter("android:imagePhotoPiece")
fun setPhotoPieceImage(imageView: ImageView, photoList: MutableList<Uri>) {
    val photoPiece = photoList.size
    if (photoPiece > 0) {
        imageView.visibility = View.VISIBLE
    } else {
        imageView.visibility = View.GONE
    }
}

@BindingAdapter("android:setDate")
fun setDateLanguage(textView: TextView, date: Diary) {
    val sharedPreferences = SharedPreferences(textView.context)
    val LANGUAGE_CODE = "languageData"
    val CALENDAR_KEY = "isFromCalendar"
    val languageCodeTr = "tr"
    val languageCode = sharedPreferences.getStringData(LANGUAGE_CODE)
    val fromCalendar = sharedPreferences.getBooleanData(CALENDAR_KEY)
    if (fromCalendar) {
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