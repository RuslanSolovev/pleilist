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
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
) {
    val trackTime: String
        get() = if (trackTimeMillis != null) {
            val minutes = trackTimeMillis / 60000
            val seconds = (trackTimeMillis % 60000) / 1000
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "--:--"
        }

    // Функция для получения ссылки на обложку высокого качества.
    fun getCoverArtwork(): String? {
        return artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")
    }
}