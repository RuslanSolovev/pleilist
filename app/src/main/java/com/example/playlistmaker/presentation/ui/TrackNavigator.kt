package com.example.playlistmaker.presentation.ui

import android.content.Context
import android.content.Intent
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.fragments.PlayerFragment

class TrackNavigator(private val context: Context) {
    fun openTrack(track: Track) {
        val intent = Intent(context, PlayerFragment::class.java).apply {
            putExtra("TRACK_ID", track.trackId)
            putExtra("TRACK_NAME", track.trackName)
            putExtra("ARTIST_NAME", track.artistName)
            putExtra("ARTWORK_URL", track.artworkUrl100)
            putExtra("COLLECTION_NAME", track.collectionName)
            putExtra("RELEASE_DATE", track.releaseDate)
            putExtra("PRIMARY_GENRE", track.primaryGenreName)
            putExtra("COUNTRY", track.country)
            putExtra("TRACK_TIME_MILLIS", track.trackTimeMillis)
            putExtra("PREVIEW_URL", track.previewUrl)
        }
        context.startActivity(intent)
    }
}
