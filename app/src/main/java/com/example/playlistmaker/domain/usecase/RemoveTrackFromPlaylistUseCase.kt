package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.PlaylistRepository

/**
 * Use case для удаления трека из плейлиста.
 * @param repository Репозиторий для работы с плейлистами.
 */
class RemoveTrackFromPlaylistUseCase(private val repository: PlaylistRepository) {

    /**
     * Удаляет трек из плейлиста.
     * @param trackId Идентификатор трека для удаления.
     * @param playlistId Идентификатор плейлиста, из которого нужно удалить трек.
     */
    suspend operator fun invoke(trackId: Int, playlistId: Long) {
        repository.removeTrackFromPlaylist(trackId, playlistId)
    }
}