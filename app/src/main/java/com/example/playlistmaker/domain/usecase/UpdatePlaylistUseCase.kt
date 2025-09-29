package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository


class UpdatePlaylistUseCase(private val playlistRepository: PlaylistRepository) {


    suspend operator fun invoke(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }
}