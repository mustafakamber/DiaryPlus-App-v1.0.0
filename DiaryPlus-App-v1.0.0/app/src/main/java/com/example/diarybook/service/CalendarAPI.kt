package com.example.diarybook.service

import com.example.diarybook.model.Calendar
import com.example.diarybook.constant.Constant.extUrl
import io.reactivex.Single
import retrofit2.http.GET

interface CalendarAPI {
    @GET(extUrl)
    fun getCalendar() : Single<List<Calendar>>
}