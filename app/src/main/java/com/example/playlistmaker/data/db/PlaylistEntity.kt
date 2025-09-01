package com.example.playlistmaker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverImagePath: String? = null,
    @SerializedName("trackIds")
    val trackIdsJson: String? = "[]",
    val tracksCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)