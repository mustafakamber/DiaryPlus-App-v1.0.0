package com.example.diarybook.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.databinding.RecyclerDiaryPhotoRowBinding
import com.example.diarybook.util.downloadImageFromUrl
import com.example.diarybook.util.placeHolderProgressBar

class DiaryPhotoAdapter(
    val notePhotos: MutableList<Uri>, val onPhotoSelected: (Uri) -> Unit
) : RecyclerView.Adapter<DiaryPhotoAdapter.DiaryPhotoHolder>() {

    class DiaryPhotoHolder(val binding: RecyclerDiaryPhotoRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryPhotoHolder {

        val diaryPhotoBinding =
            RecyclerDiaryPhotoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return DiaryPhotoHolder(diaryPhotoBinding)
    }

    override fun getItemCount(): Int {

        return notePhotos.size
    }

    override fun onBindViewHolder(holder: DiaryPhotoHolder, position: Int) = with(holder.binding) {

        if (position >= 0 && position < notePhotos.size) {

            notePhotoImage.downloadImageFromUrl(
                notePhotos[position].toString(),
                placeHolderProgressBar(root.context)
            )

            notePhotoDelete.visibility = View.GONE
            val clickListener = View.OnClickListener {
                if (notePhotoDelete.visibility == View.VISIBLE) {
                    notePhotoDelete.visibility = View.GONE
                } else {
                    notePhotoDelete.visibility = View.VISIBLE
                }
            }

            holder.itemView.setOnClickListener(clickListener)

            notePhotoDelete.setOnClickListener {
                val selectedPhoto = notePhotos[position]
                onPhotoSelected(selectedPhoto)
            }
        }
    }

}