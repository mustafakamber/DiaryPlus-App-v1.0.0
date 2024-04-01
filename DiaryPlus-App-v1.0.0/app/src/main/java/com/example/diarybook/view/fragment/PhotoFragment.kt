package com.example.diarybook.view.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.diarybook.R
import com.example.diarybook.databinding.FragmentPhotoBinding
import com.example.diarybook.constant.Constant.FULL_PHOTO_DATA
import com.example.diarybook.util.downloadImageFromUrl
import com.example.diarybook.util.placeHolderProgressBar
import kotlinx.android.synthetic.main.activity_base.*

class PhotoFragment : Fragment() {

    private lateinit var photoFragmentBinding: FragmentPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        photoFragmentBinding = FragmentPhotoBinding.inflate(layoutInflater)
        return photoFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(photoFragmentBinding) {
            super.onViewCreated(view, savedInstanceState)


            requireActivity().bottomAddNoteButton.visibility = View.GONE
            requireActivity().bottomMenu.visibility = View.GONE

            val fullPhotoFromAdapter = arguments?.getParcelable<Uri>(FULL_PHOTO_DATA)


            photoImageView.downloadImageFromUrl(
                fullPhotoFromAdapter.toString(),
                placeHolderProgressBar(requireContext())
            )

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backToSettingsFragment()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(callback)

            photoBackButton.setOnClickListener {
                backToSettingsFragment()
            }

        }

    override fun onPause() {
        super.onPause()
        requireActivity().bottomAddNoteButton.visibility = View.GONE
        requireActivity().bottomMenu.visibility = View.VISIBLE
    }


    private fun backToSettingsFragment() {

        val fragment = SettingsFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment).commit()
        }

    }

}

