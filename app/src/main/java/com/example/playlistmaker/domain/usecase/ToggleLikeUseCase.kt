package com.example.playlistmaker.domain.usecases

import com.example.playlistmaker.domain.repositories.LikeRepository

class ToggleLikeUseCase(private val repository: LikeRepository) {

    // Основной метод для переключения состояния
    operator fun invoke(trackId: String) {
        repository.toggleLike(trackId)
    }

    // Явное переключение состояния
    fun toggleLike(trackId: String, currentState: Boolean) {
        repository.toggleLike(trackId)
    }

    // Получение текущего статуса
    fun getLikeStatus(trackId: String): Boolean {
        return repository.getLikeStatus(trackId)
    }

    // Установка конкретного состояния
    fun setLikeStatus(trackId: String, isLiked: Boolean) {
        repository.setLikeStatus(trackId, isLiked)
    }
}