package com.example.diarybook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.databinding.RecyclerCalendarPhotoRowBinding
import com.example.diarybook.model.Calendar
import com.example.diarybook.util.downloadImageFromUrl
import com.example.diarybook.util.placeHolderProgressBar


class CalendarAdapter(
    val calendar: ArrayList<Calendar>

) : RecyclerView.Adapter<CalendarAdapter.CalendarPhotoHolder>() {


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

            if (position >= 0 && position < calendar.size) {

                holder.binding.homeCalenderImage.downloadImageFromUrl(calendar[position].calendarImageUrl,
                    placeHolderProgressBar(holder.itemView.context)
                )

            }

        }

    override fun getItemCount(): Int {


        return 1
    }



    fun showNextMonthImage(position: Int) {

        if (position >= 0 && position < calendar.size - 1) {

            val newList: ArrayList<Calendar> = ArrayList()

            val index = position + 1

            for (i in index until calendar.size) {
                newList.add(calendar[i])
            }
            for (i in 0 until index) {
                newList.add(calendar[i])
            }

            updateCalendarList(newList)

        }

    }


    fun updateCalendarList(newCalendarList: List<Calendar>) {
        calendar.clear()
        calendar.addAll(newCalendarList)
        notifyDataSetChanged()
    }

}