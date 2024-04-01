package com.example.diarybook.view.dialog

import android.app.DatePickerDialog
import android.content.Context
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.dateFormat
import com.example.diarybook.constant.Constant.languageCodeTr
import com.example.diarybook.constant.Constant.monthFormat
import java.text.SimpleDateFormat
import java.util.*


class DatePickerDialog (private val context : Context) {


    val myCalendar = Calendar.getInstance()
    private val sharedPreferences = SharedPreferences(context)

    fun getDatePickerDialog(
        onDateReady: (String, String) -> Unit
    ) {

        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayofMonth ->

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayofMonth)

            val noteDateEn = updateDateEn(myCalendar)
            val noteDateTr = updateDateTr(myCalendar)

            onDateReady.invoke(noteDateEn, noteDateTr)
        }

        DatePickerDialog(
            context,
            datePicker,
            myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()

    }

    private fun updateDateEn(myCalendar: Calendar): String {

        val sdf = SimpleDateFormat(dateFormat, Locale.UK)
        val date = sdf.format(myCalendar.time)

        return date
    }

    private fun updateDateTr(myCalendar: Calendar): String {

        val turkishLocale = Locale(languageCodeTr, "TR")
        val turkishDateFormat = SimpleDateFormat(dateFormat, turkishLocale)
        val turkishDate = turkishDateFormat.format(myCalendar.time)

        return turkishDate
    }

    fun currentMonth(): String {

        val sdf = SimpleDateFormat(monthFormat, Locale.UK)
        val currentMonth = sdf.format(Date())

        return currentMonth
    }

    fun currentDateEn(): String {

        val sdf = SimpleDateFormat(dateFormat, Locale.UK)
        val currentDate = sdf.format(myCalendar.time)

        return currentDate
    }

    fun currentDateTr(): String {

        val turkishLocale = Locale(languageCodeTr, "TR")
        val turkishDateFormat = SimpleDateFormat(dateFormat, turkishLocale)
        val turkishDate = turkishDateFormat.format(myCalendar.time)

        return turkishDate
    }

    fun currentDate(): String {

        val sdf = SimpleDateFormat(dateFormat, Locale.UK)
        var currentDate = sdf.format(myCalendar.time)

        val languageCode = sharedPreferences.getStringData(LANGUAGE_DATA)

        if (languageCode == languageCodeTr) {
            val turkishLocale = Locale(languageCodeTr, "TR")
            val turkishDateFormat = SimpleDateFormat(dateFormat, turkishLocale)
            val turkishDate = turkishDateFormat.format(myCalendar.time)
            currentDate = turkishDate
        }

        return currentDate
    }

     fun selectedDate(year: Int, month: Int, dayOfMonth: Int, selectedDateReady : (String,String) -> Unit) {

        val selectedYear = year
        val selectedMonth = month
        val selectedDayOfMonth = dayOfMonth
        val calendar = Calendar.getInstance()


        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)

        val dateFormatEn = SimpleDateFormat(dateFormat,Locale.UK)

        val dateEn =  dateFormatEn.format(calendar.time)

        val turkishLocale = Locale(languageCodeTr, "TR")
        val dateFormantTr = SimpleDateFormat(dateFormat,turkishLocale)

        val dateTr = dateFormantTr.format(calendar.time)

        selectedDateReady.invoke(dateEn,dateTr)
    }

}