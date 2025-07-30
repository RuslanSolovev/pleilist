package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.MediaPlayerInteractor
import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase
import com.example.playlistmaker.domain.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewModel(
    private val mediaPlayerInteractor: MediaPlayerInteractor,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val timeFormatter: TimeFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            currentTime = "--:--",
            totalTime = "--:--"
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var timeUpdateJob: Job? = null
    private var currentTrackId = -1

    init {
        mediaPlayerInteractor.setOnCompletionListener {
            stopProgressUpdates()
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    currentTime = "00:00"
                )
            }
            mediaPlayerInteractor.seekTo(0)
        }
    }

    fun preparePlayer(url: String) {
        _uiState.update { it.copy(isLoading = true) }

        mediaPlayerInteractor.prepare(
            url = url,
            onPrepared = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentTime = "00:00",
                        totalTime = formatDuration(mediaPlayerInteractor.getDuration())
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error
                    )
                }
            }
        )
    }

    private fun formatDuration(durationMs: Int): String {
        return timeFormatter.formatTrackTime(durationMs.toLong())
    }

    fun togglePlayPause() {
        if (mediaPlayerInteractor.getCurrentPosition() >= mediaPlayerInteractor.getDuration()) {
            mediaPlayerInteractor.seekTo(0)
        }

        mediaPlayerInteractor.togglePlayPause()
        val isPlaying = !_uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = isPlaying) }

        if (isPlaying) {
            startProgressUpdates()
        } else {
            stopProgressUpdates()
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        timeUpdateJob = viewModelScope.launch {
            while (true) {
                delay(300)
                _uiState.update {
                    it.copy(
                        currentTime = timeFormatter.formatTrackTime(
                            mediaPlayerInteractor.getCurrentPosition().toLong()
                        )
                    )
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = null
    }

    fun setTrackId(trackId: Int) {
        currentTrackId = trackId
        viewModelScope.launch {
            val isLiked = toggleLikeUseCase.getLikeStatus(trackId.toString())
            _uiState.update { it.copy(isLiked = isLiked) }
        }
    }

    fun toggleLike() {
        if (currentTrackId != -1) {
            val currentState = _uiState.value.isLiked
            toggleLikeUseCase.toggleLike(currentTrackId.toString(), currentState)
            _uiState.update { it.copy(isLiked = !currentState) }
        }
    }

    fun releasePlayer() {
        mediaPlayerInteractor.release()
        stopProgressUpdates()
    }
}

data class PlayerUiState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val currentTime: String,
    val totalTime: String,
    val isLiked: Boolean = false,
    val error: String? = null
)