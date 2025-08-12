package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.MediaPlayerInteractor
import com.example.playlistmaker.domain.model.Track
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
    private var currentTrack: Track? = null // Храним весь объект Track

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

    // Метод для установки данных трека
    fun setTrackData(
        trackId: Int,
        trackName: String?,
        artistName: String?,
        artworkUrl: String?,
        collectionName: String?,
        releaseDate: String?,
        primaryGenre: String?,
        country: String?,
        trackTimeMillis: Long?,
        previewUrl: String?
    ) {
        // Создаем объект Track из переданных данных
        // Все поля, кроме обязательных, заполняются данными или остаются null
        val track = Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenre,
            country = country,
            previewUrl = previewUrl
            // Поле isFavorite будет установлено позже при проверке статуса
        )

        this.currentTrack = track

        // Получаем статус "лайка" из LikeRepository через ToggleLikeUseCase
        viewModelScope.launch {
            val isLiked = toggleLikeUseCase.getLikeStatus(trackId.toString())
            _uiState.update { it.copy(isLiked = isLiked) }
        }
    }

    fun toggleLike() {
        val track = currentTrack ?: return // Нет данных о треке
        viewModelScope.launch {
            try {

                toggleLikeUseCase(track)


                val newStatus = toggleLikeUseCase.getLikeStatus(track.trackId.toString())

                _uiState.update { it.copy(isLiked = newStatus) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка при изменении лайка: ${e.message}") }

            }
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