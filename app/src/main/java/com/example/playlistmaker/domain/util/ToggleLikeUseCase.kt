// Файл: package com.example.playlistmaker.domain.usecases
package com.example.playlistmaker.domain.util

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repositories.FavoriteRepository


class ToggleLikeUseCase(private val favoriteRepository: FavoriteRepository) {


    suspend operator fun invoke(track: Track) {

        if (favoriteRepository.isFavorite(track.trackId)) {
            favoriteRepository.removeFromFavorites(track)
        } else {
            favoriteRepository.addToFavorites(track)
        }
    }

}