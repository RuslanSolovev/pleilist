package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var themeInteractor: ThemeInteractor

    override fun onCreate() {
        super.onCreate()
        applyInitialTheme()
    }

    private fun applyInitialTheme() {
        Thread {
            val isDark = themeInteractor.isDarkThemeEnabled()
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }.start()
    }
}