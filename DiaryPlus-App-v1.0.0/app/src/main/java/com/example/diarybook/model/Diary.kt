package com.example.diarybook.model

import android.net.Uri


data class Diary (
    val diaryId : String?,
    val diaryTitle : String?,
    val diaryContent : String?,
    val diaryPhoto : MutableList<Uri>?,
    val diaryDateEn : String?,
    val diaryDateTr : String?,
    val diaryBackgroundColor : String?,
    val diaryTextColor : String?,
    val diaryTime : String?,
)

