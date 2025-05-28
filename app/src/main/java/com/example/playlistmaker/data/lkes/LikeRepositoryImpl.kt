package com.example.playlistmaker.data.repository


import android.content.SharedPreferences
import com.example.playlistmaker.domain.repositories.LikeRepository

import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : LikeRepository {

    companion object {
        private const val LIKED_TRACKS_KEY = "LIKED_TRACKS"
    }

    override fun getLikeStatus(trackId: String): Boolean {
        return sharedPreferences.getStringSet(LIKED_TRACKS_KEY, emptySet())?.contains(trackId) ?: false
    }

    override fun setLikeStatus(trackId: String, isLiked: Boolean) {
        val likedTracks = sharedPreferences.getStringSet(LIKED_TRACKS_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (isLiked) {
            likedTracks.add(trackId)
        } else {
            likedTracks.remove(trackId)
        }
        sharedPreferences.edit()
            .putStringSet(LIKED_TRACKS_KEY, likedTracks)
            .apply()
    }

    override fun toggleLike(trackId: String) {
        val current = getLikeStatus(trackId)
        setLikeStatus(trackId, !current)
    }
}