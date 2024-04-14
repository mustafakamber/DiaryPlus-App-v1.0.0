package com.example.diarybook.service

import com.example.diarybook.model.Calendar
import com.example.diarybook.constant.Constant.baseUrl
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CalendarService {
    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(CalendarAPI::class.java)

    fun getCalendarData() : Single<List<Calendar>>{
        return  api.getCalendar()
    }
}