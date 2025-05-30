package com.example.yourapp.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.yourapp.domain.repository.TrackRepository

class SearchTracksUseCase(private val repository: TrackRepository) {
    suspend operator fun invoke(query: String): List<Track> {
        return repository.searchTracks(query)
    }
}

