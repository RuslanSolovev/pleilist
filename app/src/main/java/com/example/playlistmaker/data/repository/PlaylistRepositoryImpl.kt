package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.TrackForPlaylistDao
import com.example.playlistmaker.data.db.TrackForPlaylistEntity
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackForPlaylistDao: TrackForPlaylistDao,
    private val gson: Gson
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return playlistDao.insert(playlist.toEntity())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        // Сначала получаем плейлист, чтобы получить путь к обложке
        val playlistEntity = playlistDao.getPlaylistById(playlistId)
        playlistEntity?.let {
            // Удаляем запись из БД
            playlistDao.deletePlaylist(playlistId)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.update(playlist.toEntity())
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }



    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistDao.getPlaylistById(playlistId)?.toDomain()
    }

    override suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean {
        return try {
            // 1. Сохраняем трек в таблицу треков (если еще не сохранен)
            val trackEntity = TrackForPlaylistEntity(
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTimeMillis = track.trackTimeMillis,
                artworkUrl100 = track.artworkUrl100,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                previewUrl = track.previewUrl
            )

            trackForPlaylistDao.insert(trackEntity)

            // 2. Получаем текущий плейлист
            val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return false

            // 3. Обновляем список trackIds
            val currentTrackIds = if (!playlistEntity.trackIdsJson.isNullOrBlank()) {
                gson.fromJson(playlistEntity.trackIdsJson, Array<Int>::class.java).toMutableList()
            } else {
                mutableListOf()
            }

            // Проверяем, есть ли уже трек в плейлисте
            if (currentTrackIds.contains(track.trackId)) {
                return false
            }

            // Добавляем новый trackId
            currentTrackIds.add(track.trackId)

            // 4. Обновляем плейлист
            val updatedEntity = playlistEntity.copy(
                trackIdsJson = gson.toJson(currentTrackIds),
                tracksCount = currentTrackIds.size
            )

            playlistDao.update(updatedEntity)
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun isTrackInPlaylist(trackId: Int, playlistId: Long): Boolean {
        val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return false

        return if (!playlistEntity.trackIdsJson.isNullOrBlank()) {
            val trackIds = gson.fromJson(playlistEntity.trackIdsJson, Array<Int>::class.java)
            trackIds.contains(trackId)
        } else {
            false
        }
    }

    private fun Playlist.toEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            coverImagePath = this.coverImagePath,
            trackIdsJson = gson.toJson(this.trackIds),
            tracksCount = this.tracksCount,
            createdAt = this.createdAt
        )
    }

    private fun PlaylistEntity.toDomain(): Playlist {
        return Playlist(
            id = this.id,
            name = this.name,
            description = this.description,
            coverImagePath = this.coverImagePath,
            trackIds = if (!this.trackIdsJson.isNullOrBlank()) {
                try {
                    gson.fromJson(this.trackIdsJson, Array<Int>::class.java).toList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            },
            tracksCount = this.tracksCount,
            createdAt = this.createdAt
        )
    }
}