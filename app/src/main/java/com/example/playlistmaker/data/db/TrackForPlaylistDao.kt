package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackForPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: TrackForPlaylistEntity)

    @Query("SELECT * FROM tracks_for_playlists WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Int): TrackForPlaylistEntity?
}