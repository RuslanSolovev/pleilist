package com.example.playlistmaker.presentation.player

import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.domain.repository.AudioPlayer
import com.example.playlistmaker.domain.util.TimeFormatter

@Suppress("unused") // Добавляем, чтобы избежать серого выделения при использовании DI
class MediaPlayerController(
    private val audioPlayer: AudioPlayer,
    private var trackUrl: String,
    var onTimeUpdate: (String) -> Unit,
    var onPlayStateChanged: (Boolean) -> Unit,
    var onLikeStateChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var isPrepared = false
    var isLiked = false
    private var currentPosition = 0

    private val updateRunnable = object : Runnable {
        override fun run() {
            currentPosition = audioPlayer.getCurrentPosition()
            onTimeUpdate(TimeFormatter.formatTrackTime(currentPosition.toLong()))
            if (isPlaying) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun updateTrackUrl(newUrl: String) {
        trackUrl = newUrl
        isPrepared = false
    }

    fun prepare() {
        if (isPrepared) return

        audioPlayer.prepare(
            trackUrl,
            onPrepared = {
                isPrepared = true
                audioPlayer.seekTo(currentPosition)
            },
            onError = {
                isPrepared = false
                onError("Ошибка подготовки аудио")
            }
        )

        audioPlayer.setOnCompletionListener {
            handlePlaybackCompletion()
        }
    }

    fun play() {
        if (!isPlaying && isPrepared) {
            audioPlayer.start()
            isPlaying = true
            onPlayStateChanged(true)
            handler.post(updateRunnable)
        }
    }

    fun pause() {
        if (isPlaying) {
            audioPlayer.pause()
            isPlaying = false
            onPlayStateChanged(false)
            handler.removeCallbacks(updateRunnable)
            updateCurrentPosition()
        }
    }

    fun togglePlayPause() {
        if (isPlaying) pause() else play()
    }

    fun seekTo(position: Int) {
        audioPlayer.seekTo(position)
        currentPosition = position
        onTimeUpdate(TimeFormatter.formatTrackTime(position.toLong()))
    }

    fun release() {
        audioPlayer.release()
        handler.removeCallbacks(updateRunnable)
        resetState()
    }

    fun setLikeState(liked: Boolean) {
        isLiked = liked
        onLikeStateChanged(liked)
    }

    fun toggleLike() {
        isLiked = !isLiked
        onLikeStateChanged(isLiked)
    }

    fun getPlayState(): Boolean = isPlaying
    fun getLikeState(): Boolean = isLiked
    fun getCurrentPosition(): Int = currentPosition

    private fun handlePlaybackCompletion() {
        pause()
        audioPlayer.seekTo(0)
        currentPosition = 0
        onTimeUpdate(TimeFormatter.formatTrackTime(0))
    }

    private fun updateCurrentPosition() {
        currentPosition = audioPlayer.getCurrentPosition()
        onTimeUpdate(TimeFormatter.formatTrackTime(currentPosition.toLong()))
    }

    private fun resetState() {
        isPlaying = false
        isPrepared = false
        currentPosition = 0
    }
}