package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track

interface HistoryInteractor {
    fun getHistory(): List<Track>
    fun saveHistory(tracks: List<Track>)
    fun clearHistory()
    fun saveTrack(track: Track)
}
