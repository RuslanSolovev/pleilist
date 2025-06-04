package com.example.playlistmaker.presentation.player

sealed class PlayerUiState {
    data class Content(
        val isPlaying: Boolean = false,
        val currentTime: String = "00:00",
        val isLiked: Boolean = false
    ) : PlayerUiState()

    object Loading : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}