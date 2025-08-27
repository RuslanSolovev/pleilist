package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository

class CreatePlaylistUseCase(private val repository: PlaylistRepository) {
    suspend operator fun invoke(playlist: Playlist): Long {
        return repository.createPlaylist(playlist)
    }
}