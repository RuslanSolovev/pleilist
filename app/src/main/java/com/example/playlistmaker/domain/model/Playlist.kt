package com.example.playlistmaker.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverImagePath: String? = null,
    val trackIds: List<Int> = emptyList(),
    val tracksCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

