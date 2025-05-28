package com.example.playlistmaker.domain.interactor

import kotlinx.coroutines.flow.Flow

interface ThemeInteractor {
    suspend fun isDarkThemeEnabled(): Boolean
    fun setDarkThemeEnabled(enabled: Boolean)
    fun getThemeFlow(): Flow<Boolean> // новый метод
}
