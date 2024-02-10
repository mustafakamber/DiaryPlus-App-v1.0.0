package com.example.diarybook.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.databinding.ActivitySplashBinding
import com.example.diarybook.viewmodel.UserViewModel


class SplashActivity : AppCompatActivity() {

    private lateinit var splashActivityBinding: ActivitySplashBinding
    private lateinit var userViewModel : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashActivityBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashActivityBinding.root)

        userViewModel = ViewModelProvider(this@SplashActivity)[UserViewModel::class.java]

        userViewModel.currentUser({
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