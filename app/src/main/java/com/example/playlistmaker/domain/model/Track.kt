package com.example.playlistmaker.domain.model

// domain/model/Track.kt
data class Track(
    val trackId: Int,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?,
    var isFavorite: Boolean = false
) {
    val trackTime: String
        get() = if (trackTimeMillis != null) {
            val minutes = trackTimeMillis / 60000
            val seconds = (trackTimeMillis % 60000) / 1000
            String.format("%02d:%02d", minutes, seconds)
        } else "--:--"
}
