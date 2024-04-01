package com.example.diarybook.util

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

object CallBackHandler {
    fun ComponentActivity.onBackPressed(backButtonPressed: () -> Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backButtonPressed()
            }
        }
        this.onBackPressedDispatcher.addCallback(callback)
    }
}