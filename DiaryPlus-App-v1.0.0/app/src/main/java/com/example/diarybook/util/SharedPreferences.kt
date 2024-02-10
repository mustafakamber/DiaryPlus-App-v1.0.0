package com.example.diarybook.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit

class SharedPreferences{

    companion object{
        private var sharedPreferences : SharedPreferences? = null

        @Volatile
        private var instance : com.example.diarybook.util.SharedPreferences? = null
        private val lock = Any()

        val spaceString = ""

        operator fun invoke(context: Context):
                com.example.diarybook.util.SharedPreferences = instance ?: synchronized(lock){
            instance ?: initializeSharedPreferences(context).also { customSP ->
                instance = customSP
            }
        }

        private fun initializeSharedPreferences(context : Context) : com.example.diarybook.util.SharedPreferences {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return SharedPreferences()
        }
    }

    fun saveStringData(key: String, value: String) {
        sharedPreferences?.edit(commit = true){
            putString(key,value)
        }
    }

    fun saveBooleanData(key: String, value: Boolean) {
        sharedPreferences?.edit(commit = true ) {
            putBoolean(key,value)
        }
    }

    fun getStringData(key: String): String? {
        return sharedPreferences?.getString(key, spaceString)
    }

    fun getBooleanData(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences!!.getBoolean(key, defaultValue)
    }

    fun deleteData(key: String) {
        sharedPreferences?.edit(commit = true) {
            remove(key)
        }
    }

}