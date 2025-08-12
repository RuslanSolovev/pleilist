// Файл: package com.example.playlistmaker.domain.repositories
package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getAllFavorites(): Flow<List<Track>>
    suspend fun getFavoriteIds(): List<Int>
    suspend fun isFavorite(trackId: Int): Boolean

}