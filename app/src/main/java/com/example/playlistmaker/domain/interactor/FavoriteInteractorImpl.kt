// domain/interactor/FavoriteInteractorImpl.kt
package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repositories.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FavoriteInteractorImpl(
    private val repository: FavoriteRepository
) : FavoriteInteractor {

    override suspend fun toggleFavorite(track: Track) {
        if (track.isFavorite) {
            removeFromFavorites(track)
        } else {
            addToFavorites(track)
        }
        track.isFavorite = !track.isFavorite
    }

    override fun getFavorites(): Flow<List<Track>> = repository.getAllFavorites()

    override suspend fun checkFavoriteStatus(trackId: Int): Boolean {
        return repository.getAllFavorites()
            .firstOrNull()
            ?.any { it.trackId == trackId } ?: false
    }

    override suspend fun addToFavorites(track: Track) = repository.addToFavorites(track)
    override suspend fun removeFromFavorites(track: Track) = repository.removeFromFavorites(track)
}