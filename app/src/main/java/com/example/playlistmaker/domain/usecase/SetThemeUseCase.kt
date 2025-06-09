package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.interactor.ThemeInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SetThemeUseCase (
    private val interactor: ThemeInteractor
) {
    suspend operator fun invoke(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            interactor.setDarkThemeEnabled(enabled)
        }
    }
}