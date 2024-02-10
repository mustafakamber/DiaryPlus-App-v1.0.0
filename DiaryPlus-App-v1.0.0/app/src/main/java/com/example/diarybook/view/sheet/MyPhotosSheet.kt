package com.example.diarybook.view.sheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.adapter.FullPhotoAdapter
import com.example.diarybook.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class MyPhotosSheet(private val context: Context,private val settingsViewModel : SettingsViewModel) {

    private lateinit var myPhotosBottomSheet: BottomSheetDialog

     fun getMyPhotosSheet() {

        myPhotosBottomSheet = BottomSheetDialog(
            context,
            R.style.BottomSheetDialogTheme
        )

        val bottomSheetView = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_myphotos,
            null
        )

        val myPhotosBackButton = bottomSheetView.findViewById<ImageView>(R.id.myPhotosBackButton)
        val myPhotosView = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerPhotoView)
        val myPhotosNullImage = bottomSheetView.findViewById<ImageView>(R.id.myPhotosEmptyIcon)
        val myPhotosNullMessage = bottomSheetView.findViewById<TextView>(R.id.myPhotosEmptyText)

        myPhotosView.visibility = View.GONE
        myPhotosNullImage.visibility = View.GONE
        myPhotosNullMessage.visibility = View.GONE

        myPhotosBottomSheet?.setContentView(bottomSheetView)
        myPhotosBottomSheet?.show()

        myPhotosView.layoutManager = GridLayoutManager(context, 4)

        settingsViewModel.getPhotos({ myPhoto ->
            myPhotosView.visibility = View.VISIBLE
            val photoAdapter = FullPhotoAdapter(myPhoto){
                myPhotosBottomSheet.dismiss()
            }
            myPhotosView.adapter = photoAdapter
        },{
            myPhotosNullImage.visibility = View.VISIBLE
            myPhotosNullMessage.visibility = View.VISIBLE
        })

        myPhotosBackButton.setOnClickListener {
            myPhotosBottomSheet?.dismiss()
        }

    }

}