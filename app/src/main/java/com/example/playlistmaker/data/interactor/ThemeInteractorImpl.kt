package com.example.playlistmaker.data.interactor

import android.content.SharedPreferences
import com.example.playlistmaker.domain.interactor.ThemeInteractor

class ThemeInteractorImpl(
    private val sharedPreferences: SharedPreferences
) : ThemeInteractor {

    companion object {
        const val KEY_DARK_THEME = "dark_theme_enabled"
    }

    override fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    override fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_DARK_THEME, enabled)
            .apply()
    }
}
