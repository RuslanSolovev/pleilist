package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.first

class AddTrackToPlaylistUseCase(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(track: Track, playlistId: Long): Boolean {
        // Проверяем, есть ли уже трек в плейлисте
        val isAlreadyInPlaylist = playlistRepository.isTrackInPlaylist(track.trackId, playlistId)

        if (isAlreadyInPlaylist) {
            return false
        }

        // Добавляем трек в плейлист
        return playlistRepository.addTrackToPlaylist(track, playlistId)
    }
}