package com.example.oemlauncher

import android.app.Application

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MyApp", "Application onCreate start")
        try {
            // Follow system Dark/Light mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        } catch (e: Exception) {

        }
    }
}
