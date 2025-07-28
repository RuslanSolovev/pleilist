package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun execute(query: String): Flow<List<Track>>
}