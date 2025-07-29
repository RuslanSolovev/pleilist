package com.example.playlistmaker.presentation.player

sealed class PlayerUiState {
    data class Content(
        val isLoading: Boolean = false,
        val isPlaying: Boolean = false,
        val currentTime: String = "--:--",
        val totalTime: String = "--:--",
        val error: String? = null
    ) : PlayerUiState()
}

