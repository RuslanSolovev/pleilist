package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repositories.FavoriteRepository
import com.example.playlistmaker.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class HistoryInteractorImpl(
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository
) : HistoryInteractor {

    // Базовый метод, соответствующий интерфейсу
    override fun getHistory(): List<Track> {
        return historyRepository.getHistory()
    }

    // Дополнительный метод для получения истории с информацией об избранном
    suspend fun getHistoryWithFavorites(): List<Track> {
        val history = getHistory()
        val favoriteIds = favoriteRepository.getFavoriteIds()

        return history.map { track ->
            track.copy(isFavorite = favoriteIds.contains(track.trackId))
        }
    }

    override fun saveHistory(tracks: List<Track>) {
        historyRepository.saveHistory(tracks)
    }

    override fun clearHistory() {
        historyRepository.clearHistory()
    }

    override fun saveTrack(track: Track) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.removeAll { it.trackId == track.trackId }
        currentHistory.add(0, track)
        if (currentHistory.size > 10) {
            currentHistory.subList(10, currentHistory.size).clear()
        }
        saveHistory(currentHistory)
    }
}