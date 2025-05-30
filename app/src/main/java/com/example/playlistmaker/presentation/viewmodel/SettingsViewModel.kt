package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeInteractor: ThemeInteractor
) : ViewModel() {

    val darkThemeEnabled: LiveData<Boolean> = themeInteractor.getThemeFlow().asLiveData()

    private val _shareAppEvent = MutableLiveData<String>()
    val shareAppEvent: LiveData<String> = _shareAppEvent

    fun toggleTheme() {
        val current = darkThemeEnabled.value ?: false
        themeInteractor.setDarkThemeEnabled(!current)
    }

    fun onShareClicked() {
        _shareAppEvent.value = "https://practicum.yandex.ru/android-developer/"
    }
}
