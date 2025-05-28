package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchRepository {
    /**
     * Ищет аудиотреки по строке запроса и возвращает список доменных моделей.
     */
    suspend fun searchTracks(term: String): List<Track>
}
