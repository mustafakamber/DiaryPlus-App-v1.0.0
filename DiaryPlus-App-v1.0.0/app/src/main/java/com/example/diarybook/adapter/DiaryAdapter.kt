package com.example.diarybook.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.databinding.RecyclerDiaryRowBinding
import com.example.diarybook.model.Diary
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.util.Constant.ARCHIVE_KEY
import com.example.diarybook.util.Constant.NOTE_DATA
import com.example.diarybook.view.activity.DiaryActivity
import kotlinx.android.synthetic.main.recycler_diary_row.view.*
import kotlin.collections.ArrayList

class DiaryAdapter(val diaries: ArrayList<Diary>)
    : RecyclerView.Adapter<DiaryAdapter.DiaryHolder>(), DiaryClickListener {

    class DiaryHolder(val binding: RecyclerDiaryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryHolder {

        val inflater = LayoutInflater.from(parent.context)
        val colorAdapterBinding = DataBindingUtil.inflate<RecyclerDiaryRowBinding>(
            inflater,
            R.layout.recycler_diary_row,
            parent,
            false
        )

        return DiaryHolder(colorAdapterBinding)
    }

    override fun getItemCount(): Int {

        return diaries.size
    }

    override fun onBindViewHolder(holder: DiaryHolder, position: Int) = with(holder.binding) {

        diaryRow = diaries[position]
        diaryListener = this@DiaryAdapter

    }

    fun deleteDiaryFromAdapter(position: Int, onSuccess: (String) -> Unit){
        if (position >= 0 && position < diaries.size) {
            val noteId = diaries[position].diaryId
            diaries.removeAt(position)
            notifyItemRemoved(position)
            onSuccess(noteId!!)
        }
    }

    fun updateDiaryList(newDiaryList : ArrayList<Diary>){
        diaries.clear()
        diaries.addAll(newDiaryList)
        notifyDataSetChanged()
    }

    override fun onDiaryClicked(v: View) {

        val sharedPreferences = SharedPreferences(v.context as Activity)


        val fromArchive = sharedPreferences.getBooleanData(ARCHIVE_KEY)

        if (!fromArchive) {
            sharedPreferences.saveBooleanData(NOTE_DATA, false)
            sharedPreferences.saveStringData(
                "noteId",
                v.noteId.text.toString()
            )

            val intentToDiaryActivity =
                Intent(v.context as AppCompatActivity, DiaryActivity::class.java)
            v.context.startActivity(intentToDiaryActivity)
        }
    }
}

