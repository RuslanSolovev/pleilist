package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.playlistmaker.domain.repository.ThemeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ThemeRepository {

    private val _currentTheme = MutableLiveData<Boolean>().apply {
        value = getTheme()
    }

    override val currentTheme: LiveData<Boolean> = _currentTheme

    override fun getTheme(): Boolean {
        return sharedPreferences.getBoolean(THEME_KEY, false)
    }

    override fun saveTheme(isDark: Boolean) {
        sharedPreferences.edit()
            .putBoolean(THEME_KEY, isDark)
            .apply()
        _currentTheme.postValue(isDark)
    }

    companion object {
        private const val THEME_KEY = "IS_DARK_MODE"
    }
}