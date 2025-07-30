package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {
    override fun search(query: String): Flow<List<Track>> =
        repository.searchTracks(query)
            .flowOn(Dispatchers.IO)
}