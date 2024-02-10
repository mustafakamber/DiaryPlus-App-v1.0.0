package com.example.diarybook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.databinding.RecyclerCalendarPhotoRowBinding
import com.example.diarybook.view.dialog.DatePickerDialog


class CalendarPhotoAdapter(
    val calendarPhotos: ArrayList<Int>

) : RecyclerView.Adapter<CalendarPhotoAdapter.CalendarPhotoHolder>() {

    private lateinit var datePicker: DatePickerDialog

    class CalendarPhotoHolder(val binding: RecyclerCalendarPhotoRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarPhotoHolder {
        val calendarPhotoBinding =
            RecyclerCalendarPhotoRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return CalendarPhotoHolder(calendarPhotoBinding)
    }

    override fun onBindViewHolder(holder: CalendarPhotoHolder, position: Int) =
        with(holder.binding) {
            if (position >= 0 && position < calendarPhotos.size) {

                homeCalenderImage.setImageResource(calendarPhotos[position])

            }
        }

    override fun getItemCount(): Int {


        return 1
    }


    fun showNextMonthImage(position: Int) {

        if (position >= 0 && position < calendarPhotos.size - 1) {

            val newList: ArrayList<Int> = ArrayList()

            val index = position + 1

            for (i in index until calendarPhotos.size) {
                newList.add(calendarPhotos[i])
            }
            for (i in 0 until index) {
                newList.add(calendarPhotos[i])
            }

            updateCalendarList(newList)

        }

    }

    fun updateCalendarList(newCalendarList: ArrayList<Int>) {
        calendarPhotos.clear()
        calendarPhotos.addAll(newCalendarList)
        notifyDataSetChanged()
    }

}