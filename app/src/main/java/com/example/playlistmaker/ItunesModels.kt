package com.example.playlistmaker

data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<Track>
)

data class Track(
    val trackId: Int,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
) {
    val trackTime: String
        get() = if (trackTimeMillis != null) {
            val minutes = trackTimeMillis / 60000
            val seconds = (trackTimeMillis % 60000) / 1000
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "--:--"
        }
}