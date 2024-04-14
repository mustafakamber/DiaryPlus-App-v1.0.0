package com.example.diarybook.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.databinding.ActivitySplashBinding
import com.example.diarybook.util.SharedPreferences
import com.example.diarybook.util.checkAppTheme
import com.example.diarybook.util.setDarkTheme
import com.example.diarybook.util.setLightTheme
import com.example.diarybook.viewmodel.UserViewModel


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel : UserViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this@SplashActivity)[UserViewModel::class.java]

        sharedPreferences = SharedPreferences(applicationContext)

        viewModel.currentUser({
           getActivity(BaseActivity())
        }, {
           getActivity(AuthActivity())
        })
    }

    private fun getActivity(activity : Activity){
        Handler(Looper.getMainLooper()).postDelayed({
            val intentToAuthActivity = Intent(this, activity::class.java)
            startActivity(intentToAuthActivity)
            finish()
        }, 1500)
    }
}