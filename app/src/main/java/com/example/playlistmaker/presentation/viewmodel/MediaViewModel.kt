package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.MediaPlayerInteractor
import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase
import com.example.playlistmaker.presentation.player.PlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaPlayerInteractor: MediaPlayerInteractor,
    private val toggleLikeUseCase: ToggleLikeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Content())
    val uiState: StateFlow<PlayerUiState> = _uiState

    private var currentTrackId = -1
    private var timeUpdateJob: Job? = null

    init {
        setupPlayerListeners()
    }

    private fun setupPlayerListeners() {
        mediaPlayerInteractor.setOnCompletionListener {
            stopTimeUpdates()
            updateUiState(
                isPlaying = false,
                currentTime = "00:00"
            )
            mediaPlayerInteractor.seekTo(0)
        }
    }

    private fun startTimeUpdates() {
        stopTimeUpdates()
        timeUpdateJob = viewModelScope.launch {
            while (true) {
                updateUiState(
                    currentTime = mediaPlayerInteractor.getFormattedCurrentPosition()
                )
                delay(1000)
            }
        }
    }

    private fun stopTimeUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = null
    }

    private fun updateUiState(
        isPlaying: Boolean? = null,
        currentTime: String? = null,
        isLiked: Boolean? = null,
        error: String? = null
    ) {
        _uiState.update { currentState ->
            when (currentState) {
                is PlayerUiState.Content -> currentState.copy(
                    isPlaying = isPlaying ?: currentState.isPlaying,
                    currentTime = currentTime ?: currentState.currentTime,
                    isLiked = isLiked ?: currentState.isLiked
                )
                else -> PlayerUiState.Content(
                    isPlaying = isPlaying ?: false,
                    currentTime = currentTime ?: "00:00",
                    isLiked = isLiked ?: false
                )
            }
        }
    }

    fun setTrackId(trackId: Int) {
        currentTrackId = trackId
        viewModelScope.launch {
            val isLiked = toggleLikeUseCase.getLikeStatus(trackId.toString())
            updateUiState(isLiked = isLiked)
        }
    }

    fun toggleLike() {
        if (currentTrackId != -1) {
            val currentState = (_uiState.value as? PlayerUiState.Content)?.isLiked ?: false
            val newState = !currentState
            toggleLikeUseCase.toggleLike(currentTrackId.toString(), currentState)
            updateUiState(isLiked = newState)
        }
    }

    fun preparePlayer(url: String) {
        mediaPlayerInteractor.prepare(
            url = url,
            onPrepared = {
                startTimeUpdates()
                updateUiState(
                    isPlaying = false,
                    currentTime = "00:00"
                )
            },
            onError = { error ->
                _uiState.value = PlayerUiState.Error(error)
            }
        )
    }

    fun togglePlayPause() {
        if (mediaPlayerInteractor.getCurrentPosition() >= mediaPlayerInteractor.getDuration()) {
            mediaPlayerInteractor.seekTo(0)
        }

        mediaPlayerInteractor.togglePlayPause()
        val isPlaying = (_uiState.value as? PlayerUiState.Content)?.isPlaying != true
        updateUiState(isPlaying = isPlaying)

        if (isPlaying) {
            startTimeUpdates()
        } else {
            stopTimeUpdates()
        }
    }

    fun release() {
        mediaPlayerInteractor.release()
        stopTimeUpdates()
    }
}