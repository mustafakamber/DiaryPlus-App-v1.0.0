package com.example.diarybook.model

import com.google.gson.annotations.SerializedName

data class Calendar(
    @SerializedName("name")
    val calendarName: String?,
    @SerializedName("imageUrl")
    val calendarImageUrl : String?
)


