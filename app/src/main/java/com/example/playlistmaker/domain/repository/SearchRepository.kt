package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchRepository {

    suspend fun searchTracks(term: String): List<Track>
}
