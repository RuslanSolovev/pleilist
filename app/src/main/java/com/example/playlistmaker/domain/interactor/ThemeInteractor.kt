package com.example.playlistmaker.domain.interactor


import kotlinx.coroutines.flow.StateFlow

interface ThemeInteractor {
    suspend fun isDarkThemeEnabled(): Boolean
    fun setDarkThemeEnabled(enabled: Boolean)
    fun getThemeFlow(): StateFlow<Boolean>
}
