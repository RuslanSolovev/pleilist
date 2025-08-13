// Файл: package com.example.playlistmaker.data.repository
package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.FavoriteTracksDao
import com.example.playlistmaker.data.mapper.toDomain
import com.example.playlistmaker.data.mapper.toEntity
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repositories.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class FavoriteRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoriteRepository {

    override suspend fun addToFavorites(track: Track) {
        dao.insert(track.toEntity())
    }

    override suspend fun removeFromFavorites(track: Track) {

        dao.deleteByTrackId(track.trackId)
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getFavoriteIds(): List<Int> {

        return dao.getAll().firstOrNull()?.map { it.trackId } ?: emptyList()
    }


    override suspend fun isFavorite(trackId: Int): Boolean {
        return dao.getFavoriteById(trackId) != null
    }
}