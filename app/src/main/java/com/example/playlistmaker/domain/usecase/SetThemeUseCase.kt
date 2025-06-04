package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.interactor.ThemeInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val interactor: ThemeInteractor
) {
    suspend operator fun invoke(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            interactor.setDarkThemeEnabled(enabled)
        }
    }
}