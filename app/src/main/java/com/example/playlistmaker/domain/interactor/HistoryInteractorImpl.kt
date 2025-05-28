package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.HistoryRepository

class HistoryInteractorImpl(
    private val repository: HistoryRepository
) : HistoryInteractor {
    override fun getHistory(): List<Track> = repository.getHistory()
    override fun saveHistory(tracks: List<Track>) = repository.saveHistory(tracks)
    override fun clearHistory() = repository.clearHistory()
    override fun saveTrack(track: Track) {
        val currentHistory = getHistory().toMutableList()

        // Удаляем трек с таким же id, если он уже есть
        currentHistory.removeAll { it.trackId == track.trackId }


        // Добавляем трек в начало
        currentHistory.add(0, track)

        // Ограничиваем длину истории до 10
        if (currentHistory.size > 10) {
            currentHistory.subList(10, currentHistory.size).clear()
        }

        saveHistory(currentHistory)
    }

}

