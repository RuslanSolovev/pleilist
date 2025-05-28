package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Устанавливаем тему при запуске приложения
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_theme_enabled", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}