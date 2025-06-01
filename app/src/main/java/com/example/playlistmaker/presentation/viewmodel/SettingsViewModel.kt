package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.presentation.utils.SingleLiveEvent
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeInteractor: ThemeInteractor,
) : ViewModel() {

    private val _shareEvent = SingleLiveEvent<String>()
    val shareEvent: LiveData<String> = _shareEvent

    private val _supportEvent = SingleLiveEvent<Unit>()
    val supportEvent: LiveData<Unit> = _supportEvent

    private val _termsEvent = SingleLiveEvent<String>()
    val termsEvent: LiveData<String> = _termsEvent

    fun isDarkThemeEnabled(): Boolean = themeInteractor.isDarkThemeEnabled()

    fun toggleTheme(isEnabled: Boolean) {
        themeInteractor.setDarkThemeEnabled(isEnabled)
    }

    fun onShareClicked() {
        _shareEvent.value = "Check out this cool app!"
    }

    fun onSupportClicked() {
        _supportEvent.call()
    }

    fun onTermsClicked() {
        _termsEvent.value = "https://example.com/terms"
    }
}
