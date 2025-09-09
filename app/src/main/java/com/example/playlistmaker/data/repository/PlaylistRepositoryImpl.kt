package com.example.playlistmaker.data.repository

import android.util.Log
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

    companion object {
        private const val TAG = "PlaylistRepoImpl"
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val playlistEntity = playlist.toEntity()
        return playlistDao.insert(playlistEntity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val playlistEntity = playlist.toEntity()
        playlistDao.update(playlistEntity)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val playlistEntity = playlistDao.getPlaylistById(playlistId)
        return playlistEntity?.toDomain()
    }

    override suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean {
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
            return false // Трек уже добавлен
        }

        // Добавляем новый trackId
        currentTrackIds.add(track.trackId)

        // 4. Обновляем плейлист
        val updatedEntity = playlistEntity.copy(
            trackIdsJson = gson.toJson(currentTrackIds),
            tracksCount = currentTrackIds.size
        )
        playlistDao.update(updatedEntity)
        return true
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

    // --- НОВЫЙ МЕТОД: Удаление трека из плейлиста ---
    override suspend fun removeTrackFromPlaylist(trackId: Int, playlistId: Long) {
        // 1. Получаем текущий плейлист
        val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return

        // 2. Обновляем список trackIds
        val currentTrackIds = if (!playlistEntity.trackIdsJson.isNullOrBlank()) {
            gson.fromJson(playlistEntity.trackIdsJson, Array<Int>::class.java).toMutableList()
        } else {
            mutableListOf()
        }

        // 3. Удаляем trackId из списка
        val wasRemoved = currentTrackIds.remove(Integer.valueOf(trackId)) // Integer.valueOf для корректного сравнения boxed Int

        if (wasRemoved) {
            // 4. Обновляем плейлист в БД
            val updatedEntity = playlistEntity.copy(
                trackIdsJson = gson.toJson(currentTrackIds),
                tracksCount = currentTrackIds.size
            )
            playlistDao.update(updatedEntity)
            Log.d(TAG, "Track ID $trackId removed from playlist $playlistId DB record.")

            // 5. Анализируем, используется ли трек где-либо еще
            val allPlaylists = getAllPlaylistsSync()
            val isTrackUsedElsewhere = allPlaylists.any { playlist ->
                playlist.id != playlistId && playlist.trackIds.contains(trackId)
            }

            // 6. Если трек нигде больше не используется, удаляем его из таблицы tracks_for_playlists
            if (!isTrackUsedElsewhere) {
                trackForPlaylistDao.deleteByTrackId(trackId)
                Log.d(TAG, "Track ID $trackId deleted from tracks_for_playlists table (orphaned).")
            } else {
                Log.d(TAG, "Track ID $trackId is still used in other playlists, not deleting from tracks_for_playlists.")
            }
        } else {
            Log.d(TAG, "Track ID $trackId was not found in playlist $playlistId, nothing to remove.")
        }
    }
    // -------------------------------------------------

    override suspend fun getTracksForPlaylist(trackIds: List<Int>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()
        val trackEntities = trackForPlaylistDao.getTracksByIds(trackIds)
        return trackEntities.map { it.toDomain() }
    }

    // --- НОВЫЙ МЕТОД: Получение всех плейлистов синхронно (для анализа) ---
    override suspend fun getAllPlaylistsSync(): List<Playlist> {
        // Предполагается, что playlistDao.getAllPlaylistsSync() возвращает List<PlaylistEntity>
        // Если такого метода нет, см. пункт 5 ниже.
        return try {
            playlistDao.getAllPlaylistsSync().map { it.toDomain() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all playlists sync", e)
            emptyList() // Или выбросить исключение, если это критично
        }
    }
    // -----------------------------------------------------------------------

    // --- Вспомогательные функции преобразования ---
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
                    Log.e(TAG, "Error parsing track IDs JSON for playlist ${this.id}", e)
                    emptyList()
                }
            } else {
                emptyList()
            },
            tracksCount = this.tracksCount,
            createdAt = this.createdAt
        )
    }

    private fun TrackForPlaylistEntity.toDomain(): Track {
        return Track(
            trackId = this.trackId,
            trackName = this.trackName,
            artistName = this.artistName,
            trackTimeMillis = this.trackTimeMillis,
            artworkUrl100 = this.artworkUrl100,
            collectionName = this.collectionName,
            releaseDate = this.releaseDate,
            primaryGenreName = this.primaryGenreName,
            country = this.country,
            previewUrl = this.previewUrl,
            isFavorite = false // isFavorite не относится к трекам в плейлистах
        )
    }
    // --------------------------------------------------
}