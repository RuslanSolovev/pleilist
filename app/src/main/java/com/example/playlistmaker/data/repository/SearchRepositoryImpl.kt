// Файл: package com.example.playlistmaker.data.repository
package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.mapper.toDomain
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.repositories.FavoriteRepository // Добавьте этот импорт
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Измените конструктор: favoriteTracksDao -> favoriteRepository
class SearchRepositoryImpl(
    private val apiService: ItunesApiService,
    private val favoriteRepository: FavoriteRepository // Изменено
) : SearchRepository {

    override fun searchTracks(term: String): Flow<List<Track>> = flow {
        val response = apiService.search(term)
        if (response.isSuccessful) {
            // Измените способ получения ID: используйте метод из FavoriteRepository
            val favoriteIds = favoriteRepository.getFavoriteIds() // Изменено
            val tracks = response.body()?.results?.map {
                it.toDomain().apply {
                    isFavorite = favoriteIds.contains(trackId)
                }
            } ?: emptyList()
            emit(tracks)
        } else {
            emit(emptyList())
        }
    }
}