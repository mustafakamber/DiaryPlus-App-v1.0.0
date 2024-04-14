package com.example.diarybook.util

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

object CallBackHandler {
    fun Fragment.onBackPressed(backButtonPressed: () -> Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backButtonPressed()
            }
        }
        this.requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

}