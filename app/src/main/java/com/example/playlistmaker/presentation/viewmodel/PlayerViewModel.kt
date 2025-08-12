package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.AudioPlayerInteractor

class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor
) : ViewModel() {
    // Логика для управления воспроизведением
}