package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.interactor.ThemeInteractor
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val interactor: ThemeInteractor
) {
    operator fun invoke(enabled: Boolean) {
        interactor.setDarkThemeEnabled(enabled)
    }
}
