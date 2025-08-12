package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.db.FavoriteTrackEntity
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.domain.model.Track

fun TrackDto.toDomain(): Track {
    return Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
        isFavorite = false // По умолчанию трек не в избранном
    )
}

fun Track.toEntity(): FavoriteTrackEntity = FavoriteTrackEntity(
    trackId = this.trackId,
    trackName = this.trackName,
    artistName = this.artistName,
    trackTimeMillis = this.trackTimeMillis,
    artworkUrl100 = this.artworkUrl100,
    collectionName = this.collectionName,
    releaseDate = this.releaseDate,
    primaryGenreName = this.primaryGenreName,
    country = this.country,
    previewUrl = this.previewUrl
)

fun FavoriteTrackEntity.toDomain(): Track = Track(
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
    isFavorite = true // При преобразовании из Entity ставим true
)