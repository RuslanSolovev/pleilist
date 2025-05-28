// domain/repository/SearchHistoryRepository.kt
package com.example.domain.repository


import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun saveHistory(newHistory: List<Track>)
    fun clearHistory()
}
