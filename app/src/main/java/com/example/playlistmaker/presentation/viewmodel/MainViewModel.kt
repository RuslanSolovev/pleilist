package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {
    private val themeInteractor: ThemeInteractor by inject()
}