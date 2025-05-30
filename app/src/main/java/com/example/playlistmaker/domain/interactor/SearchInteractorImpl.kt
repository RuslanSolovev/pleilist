package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {
    override suspend fun execute(query: String): List<Track> {
        return repository.searchTracks(query)
    }
}
