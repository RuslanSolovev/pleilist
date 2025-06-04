package com.example.playlistmaker.presentation.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.SupportInteractor
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.presentation.utils.SettingsEvent
import com.example.playlistmaker.presentation.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeInteractor: ThemeInteractor,
    private val supportInteractor: SupportInteractor
) : ViewModel() {

    private val _event = SingleLiveEvent<SettingsEvent>()
    val event: LiveData<SettingsEvent> = _event

    fun getCurrentTheme(): Boolean {
        return themeInteractor.isDarkThemeEnabled()
    }

    fun updateTheme(enabled: Boolean) {
        themeInteractor.setDarkThemeEnabled(enabled)
    }

    fun shareApp() {
        _event.value = SettingsEvent.Share(supportInteractor.getShareIntentText())
    }

    fun contactSupport() {
        val (email, subject) = supportInteractor.getSupportEmailIntentData()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        val errorMessage = "No email client installed"
        _event.value = SettingsEvent.Support(intent, errorMessage)
    }

    fun openTerms() {
        _event.value = SettingsEvent.Terms(supportInteractor.getTermsIntentUrl())
    }
}