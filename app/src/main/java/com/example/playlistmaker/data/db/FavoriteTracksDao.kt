// Файл: package com.example.playlistmaker.data.db (примерное содержимое)
package com.example.playlistmaker.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {
    @Query("SELECT * FROM favorite_tracks ORDER BY trackId DESC") // Сортировка по trackId
    fun getAll(): Flow<List<FavoriteTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: FavoriteTrackEntity)


    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Int)


    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId LIMIT 1")
    suspend fun getFavoriteById(trackId: Int): FavoriteTrackEntity?


    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getAllTrackIds(): List<Int>
}