package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {
    override fun execute(query: String): Flow<List<Track>> {
        return flow {
            if (query.isNotEmpty()) {
                val result = repository.searchTracks(query)
                emit(result)
            } else {
                emit(emptyList())
            }
        }.flowOn(Dispatchers.IO)
    }
}