package com.example.diarybook.view.fragment

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.FULL_PHOTO_DATA
import com.example.diarybook.databinding.FragmentPhotoBinding
import com.example.diarybook.util.CallBackHandler.onBackPressed
import com.example.diarybook.util.downloadImageFromUrl
import com.example.diarybook.util.placeHolderProgressBar
import kotlinx.android.synthetic.main.activity_base.*

class PhotoFragment : Fragment() {

    private lateinit var binding: FragmentPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBackPressed {
            backToSettingsFragment()
        }

        setupPhotoScreen()

    }

    override fun onPause() {
        super.onPause()
        requireActivity().bottomAddNoteButton.visibility = View.GONE
        requireActivity().bottomMenu.visibility = View.VISIBLE
    }

    private fun setupPhotoScreen() = with(binding) {

        requireActivity().bottomAddNoteButton.visibility = View.GONE
        requireActivity().bottomMenu.visibility = View.GONE

        val fullPhotoFromAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(FULL_PHOTO_DATA, Uri::class.java)
        } else {
            arguments?.getParcelable(FULL_PHOTO_DATA)
        }

        photoImageView.downloadImageFromUrl(
            fullPhotoFromAdapter.toString(),
            placeHolderProgressBar(requireContext())
        )

        photoBackButton.setOnClickListener {
            backToSettingsFragment()
        }
    }

    private fun backToSettingsFragment() {

        val fragment = SettingsFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment).commit()
        }
    }

}

