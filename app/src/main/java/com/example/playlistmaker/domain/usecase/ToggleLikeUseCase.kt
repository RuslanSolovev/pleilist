// Файл: package com.example.playlistmaker.domain.usecases
package com.example.playlistmaker.domain.usecases

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repositories.FavoriteRepository

class ToggleLikeUseCase(private val favoriteRepository: FavoriteRepository) {

    // Основной метод для переключения состояния, работает с объектом Track
    suspend operator fun invoke(track: Track) {
        if (favoriteRepository.isFavorite(track.trackId)) {
            // println("DEBUG: Removing track ${track.trackName} from favorites")
            favoriteRepository.removeFromFavorites(track)
        } else {
            // println("DEBUG: Adding track ${track.trackName} to favorites")
            favoriteRepository.addToFavorites(track)
        }
    }

    // Получение текущего статуса по ID
    suspend fun getLikeStatus(trackId: String): Boolean {
        return try {
            favoriteRepository.isFavorite(trackId.toInt())
        } catch (e: NumberFormatException) {
            false
        }
    }

    // Геттер для репозитория, чтобы MediaViewModel мог проверить статус после операции
    // (альтернатива - добавить suspend fun isFavorite(trackId: Int) в сам UseCase)
    val repository: FavoriteRepository get() = favoriteRepository
}