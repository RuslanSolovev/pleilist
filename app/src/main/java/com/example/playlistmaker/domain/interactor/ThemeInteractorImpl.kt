package com.example.playlistmaker.domain.interactor

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeInteractorImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ThemeInteractor {

    companion object {
        private const val KEY_DARK_THEME = "dark_theme_enabled"
    }

    private val _themeFlow = MutableStateFlow(sharedPreferences.getBoolean(KEY_DARK_THEME, false))
    override fun getThemeFlow(): StateFlow<Boolean> = _themeFlow.asStateFlow()

    override suspend fun isDarkThemeEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    override fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _themeFlow.value = enabled
    }
}
