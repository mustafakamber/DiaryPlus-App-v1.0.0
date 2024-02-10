package com.example.diarybook.adapter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.databinding.RecyclerFullPhotoRowBinding
import com.example.diarybook.util.Constant.FULL_PHOTO_DATA
import com.example.diarybook.util.downloadImageFromUrl
import com.example.diarybook.util.placeHolderProgressBar
import com.example.diarybook.view.fragment.PhotoFragment
import com.example.diarybook.view.fragment.SettingsFragmentDirections


class FullPhotoAdapter(val myPhotos: MutableList<Uri>,
                       val onPhotoSelected: () -> Unit) :
    RecyclerView.Adapter<FullPhotoAdapter.PhotoHolder>(){

    class PhotoHolder(val binding: RecyclerFullPhotoRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {

        val fullPhotoBinding = RecyclerFullPhotoRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return PhotoHolder(fullPhotoBinding)
    }

    override fun getItemCount(): Int {
        return myPhotos.size
    }


    override fun onBindViewHolder(holder: PhotoHolder, position: Int) = with(holder.binding) {
        myPhotoImage.downloadImageFromUrl(myPhotos[position].toString(),
            placeHolderProgressBar(holder.itemView.context)
        )
        myPhotoImage.setOnClickListener {


            val data = myPhotos[position]
            val bundle = Bundle()
            bundle.putParcelable(FULL_PHOTO_DATA, data)
            val fragment = PhotoFragment()
            fragment.arguments = bundle
            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            fragmentManager.apply {
                beginTransaction()
                    .replace(R.id.baseFragment, fragment)
                    .commit()
            }

            onPhotoSelected()
        }
    }

}