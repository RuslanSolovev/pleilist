package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.PlaylistRepository

class DeletePlaylistUseCase(private val repository: PlaylistRepository) {
    suspend operator fun invoke(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }
}