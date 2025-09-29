package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository

class GetTracksForPlaylistUseCase(private val repository: PlaylistRepository) {
    suspend operator fun invoke(trackIds: List<Int>): List<Track> {
        return repository.getTracksForPlaylist(trackIds)
    }
}