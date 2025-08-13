// domain/interactor/SearchInteractorImpl.kt
package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repositories.FavoriteRepository
import com.example.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SearchInteractorImpl(
    private val searchRepository: SearchRepository,
    private val favoriteRepository: FavoriteRepository
) : SearchInteractor {

    override fun search(query: String): Flow<List<Track>> {
        return searchRepository.searchTracks(query)
            .combine(favoriteRepository.getAllFavorites()) { searchResults, favorites ->
                searchResults.map { track ->
                    track.copy(isFavorite = favorites.any { it.trackId == track.trackId })
                }
            }
    }
}