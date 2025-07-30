package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchTracks(term: String): Flow<List<Track>>
}