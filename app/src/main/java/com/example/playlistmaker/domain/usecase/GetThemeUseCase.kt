package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.interactor.ThemeInteractor
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val interactor: ThemeInteractor
) {
    operator fun invoke(): Boolean {
        return interactor.isDarkThemeEnabled()
    }
}