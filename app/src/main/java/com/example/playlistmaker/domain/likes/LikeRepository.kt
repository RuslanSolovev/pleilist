package com.example.playlistmaker.domain.repositories

interface LikeRepository {
    fun getLikeStatus(trackId: String): Boolean
    fun setLikeStatus(trackId: String, liked: Boolean)
    fun toggleLike(trackId: String)
}