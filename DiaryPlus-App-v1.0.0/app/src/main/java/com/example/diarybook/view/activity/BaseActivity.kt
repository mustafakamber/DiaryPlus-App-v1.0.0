package com.example.diarybook.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.diarybook.R
import com.example.diarybook.databinding.ActivityBaseBinding
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.NOTE_DATA
import com.example.diarybook.util.setAppLanguage
import com.example.diarybook.view.fragment.ArchiveFragment
import com.example.diarybook.view.fragment.CalendarFragment
import com.example.diarybook.view.fragment.HomeFragment
import com.example.diarybook.view.fragment.SettingsFragment


class BaseActivity : AppCompatActivity() {

    private lateinit var baseActivityBinding: ActivityBaseBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivityBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(baseActivityBinding.root)
        with(baseActivityBinding) {

            sharedPreferences = SharedPreferences(applicationContext)

            val languageCode = sharedPreferences.getStringData(LANGUAGE_DATA)
            languageCode?.setAppLanguage(this@BaseActivity)

            replaceFragment(HomeFragment())



            bottomAddNoteButton.setOnClickListener {
                getDiaryActivityToAddNote()
            }

            bottomMenuView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.homeButton -> {
                        replaceFragment(HomeFragment())
                    }
                    R.id.calendarButton -> {
                        replaceFragment(CalendarFragment())
                    }
                    R.id.archiveButton -> {
                        replaceFragment(ArchiveFragment())
                    }
                    R.id.settingsButton -> {
                        replaceFragment(SettingsFragment())
                    }
                    else -> {

                    }
                }
                true
            }


        }

    }

    private fun getDiaryActivityToAddNote(){
        sharedPreferences.saveBooleanData(NOTE_DATA, true)
        val intentToDiaryActivity = Intent(this, DiaryActivity::class.java)
        startActivity(intentToDiaryActivity)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment)
            commit()
        }
    }

}