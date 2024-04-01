package com.example.diarybook.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.diarybook.R
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.util.setAppLanguage


class AuthActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        sharedPreferences = SharedPreferences(applicationContext)

        val languageCode = sharedPreferences.getStringData(LANGUAGE_DATA)
        languageCode?.setAppLanguage(this)
    }
}