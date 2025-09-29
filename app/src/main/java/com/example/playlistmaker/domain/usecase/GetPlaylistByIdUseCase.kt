package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository

class GetPlaylistByIdUseCase(private val repository: PlaylistRepository) {
    suspend operator fun invoke(playlistId: Long): Playlist? {
        return repository.getPlaylistById(playlistId)
    }
}