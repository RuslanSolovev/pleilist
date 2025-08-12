// domain/interactor/FavoriteInteractor.kt
package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteInteractor {
    suspend fun toggleFavorite(track: Track)
    fun getFavorites(): Flow<List<Track>>
    suspend fun checkFavoriteStatus(trackId: Int): Boolean
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
}