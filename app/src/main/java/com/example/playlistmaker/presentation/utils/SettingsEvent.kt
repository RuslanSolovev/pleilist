package com.example.playlistmaker.presentation.utils

import android.content.Intent

sealed class SettingsEvent {
    data class Share(val text: String) : SettingsEvent()
    data class Support(val intent: Intent, val errorMessage: String) : SettingsEvent()
    data class Terms(val url: String) : SettingsEvent()
}