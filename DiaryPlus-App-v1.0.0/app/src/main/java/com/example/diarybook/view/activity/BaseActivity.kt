package com.example.diarybook.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.ARCHIVE_KEY
import com.example.diarybook.constant.Constant.CALENDAR_KEY
import com.example.diarybook.constant.Constant.LANGUAGE_DATA
import com.example.diarybook.constant.Constant.NOTE_DATA
import com.example.diarybook.constant.Constant.THEME_KEY
import com.example.diarybook.databinding.ActivityBaseBinding
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.util.setAppLanguage
import com.example.diarybook.view.fragment.ArchiveFragment
import com.example.diarybook.view.fragment.CalendarFragment
import com.example.diarybook.view.fragment.HomeFragment
import com.example.diarybook.view.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_base.bottomMenuView


class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = SharedPreferences(applicationContext)
        val isFromThemeButton = sharedPreferences.getBooleanData(THEME_KEY)
        if (isFromThemeButton) {
            replaceFragment(SettingsFragment())
            sharedPreferences.saveBooleanData(THEME_KEY, false)
        } else {
            replaceFragment(HomeFragment())
        }
        setupBaseScreen()
    }

    private fun setupBaseScreen() = with(binding) {
        val languageCode = sharedPreferences.getStringData(LANGUAGE_DATA)
        languageCode?.setAppLanguage(this@BaseActivity)

        bottomAddNoteButton.setOnClickListener {
            navigateDiaryScreenToAddDiary()
        }

        bottomMenuView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeButton -> replaceFragment(HomeFragment())
                R.id.calendarButton -> replaceFragment(CalendarFragment())
                R.id.archiveButton -> replaceFragment(ArchiveFragment())
                R.id.settingsButton -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    private fun navigateDiaryScreenToAddDiary() {
        sharedPreferences.saveBooleanData(NOTE_DATA, true)
        val intentToDiaryActivity = Intent(this, DiaryActivity::class.java)
        startActivity(intentToDiaryActivity)
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.baseFragment)
        when (currentFragment) {
            is CalendarFragment, is ArchiveFragment, is SettingsFragment -> {
                binding.bottomMenuView.selectedItemId = R.id.homeButton
                replaceFragment(HomeFragment())
            }
            else -> super.onBackPressed()
        }
    }

    private fun manageSpecialState(fragment : Fragment){
        val isSettingsFragment = fragment is SettingsFragment
        val isCalendarFragment = fragment is CalendarFragment
        val isArchiveFragment = fragment is ArchiveFragment
        binding.bottomAddNoteButton.visibility = if (isSettingsFragment) View.GONE else View.VISIBLE
        sharedPreferences.saveBooleanData(CALENDAR_KEY,isCalendarFragment)
        sharedPreferences.saveBooleanData(ARCHIVE_KEY,isArchiveFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        manageSpecialState(fragment)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment)
            commit()
        }
    }

}