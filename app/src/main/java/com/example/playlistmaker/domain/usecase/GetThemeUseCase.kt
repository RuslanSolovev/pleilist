package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.interactor.ThemeInteractor


class GetThemeUseCase (
    private val interactor: ThemeInteractor
) {
    operator fun invoke(): Boolean {
        return interactor.isDarkThemeEnabled()
    }
}