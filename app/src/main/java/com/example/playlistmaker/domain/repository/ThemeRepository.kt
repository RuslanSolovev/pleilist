package com.example.playlistmaker.domain.repository

import androidx.lifecycle.LiveData

interface ThemeRepository {
    val currentTheme: LiveData<Boolean>  // LiveData для наблюдения
    fun getTheme(): Boolean             // Метод для синхронного получения
    fun saveTheme(isDark: Boolean)
}