package com.example.playlistmaker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Установка темы убрана, теперь тема управляется через ThemeInteractor и ViewModel
    }
}
