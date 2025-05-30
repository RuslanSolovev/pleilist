package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.*
import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase

import com.example.playlistmaker.presentation.player.MediaPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaPlayerController: MediaPlayerController,
    private val toggleLikeUseCase: ToggleLikeUseCase
) : ViewModel() {

    private val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked

    private val _currentTime = MutableStateFlow("0:00")
    val currentTime: StateFlow<String> = _currentTime

    private val _playState = MutableLiveData<Boolean>(false)
    val playState: LiveData<Boolean> = _playState

    private var currentTrackId = -1

    init {
        mediaPlayerController.onTimeUpdate = { time ->
            _currentTime.value = time
        }

        mediaPlayerController.onPlayStateChanged = { playing ->
            _playState.postValue(playing)
        }

        mediaPlayerController.onLikeStateChanged = { liked ->
            _isLiked.postValue(liked)
        }
    }

    fun preparePlayer(url: String) {
        mediaPlayerController.updateTrackUrl(url) // Сначала обновляем URL
        mediaPlayerController.prepare() // Затем готовим плеер
    }



    fun setTrackId(id: Int) {
        currentTrackId = id
        // Загружаем сохраненное состояние лайка
        _isLiked.postValue(toggleLikeUseCase.getLikeStatus(id.toString()))
    }


    fun togglePlayPause() {
        mediaPlayerController.togglePlayPause()
    }

    fun toggleLike() {
        if (currentTrackId != -1) {
            val currentState = _isLiked.value ?: false
            val newState = !currentState

            // 1. Сохраняем в репозиторий
            toggleLikeUseCase.toggleLike(currentTrackId.toString(), currentState)

            // 2. Обновляем состояние в контроллере
            mediaPlayerController.setLikeState(newState)

            // 3. Обновляем LiveData
            _isLiked.postValue(newState)
        }
    }

    fun seekTo(position: Int) {
        mediaPlayerController.seekTo(position)
    }

    fun release() {
        mediaPlayerController.release()
    }

    fun isLiked(): Boolean = _isLiked.value ?: false
}