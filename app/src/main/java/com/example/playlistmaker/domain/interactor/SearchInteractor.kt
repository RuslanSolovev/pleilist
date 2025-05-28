package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository

interface SearchInteractor {
    suspend fun execute(query: String): List<Track>
}
