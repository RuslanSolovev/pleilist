package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.AudioPlayer
import javax.inject.Inject

class AudioPlayerInteractor @Inject constructor(
    private val audioPlayer: AudioPlayer
) {
    private var isPrepared = false
    private var isPlaying = false
    private var trackUrl: String? = null

    // Единственный внешний коллбэк для уведомления об окончании трека
    private var onCompletionListener: (() -> Unit)? = null

    fun updateTrackUrl(newUrl: String) {
        trackUrl = newUrl
        isPrepared = false
        isPlaying = false
    }

    fun prepare(onPrepared: () -> Unit, onError: () -> Unit) {
        if (isPrepared || trackUrl == null) return

        audioPlayer.prepare(
            trackUrl!!,
            onPrepared = {
                isPrepared = true
                isPlaying = false
                audioPlayer.seekTo(0)
                onPrepared()
            },
            onError = {
                isPrepared = false
                isPlaying = false
                onError()
            }
        )

        // Регистрируем слушатель окончания воспроизведения только один раз
        audioPlayer.setOnCompletionListener {
            isPlaying = false
            audioPlayer.seekTo(0)
            onCompletionListener?.invoke()
        }
    }

    fun play() {
        if (!isPrepared) return
        audioPlayer.start()
        isPlaying = true
    }

    fun pause() {
        audioPlayer.pause()
        isPlaying = false
    }

    fun togglePlayPause() {
        if (isPlaying) pause() else play()
    }

    fun seekTo(position: Int) {
        audioPlayer.seekTo(position)
    }

    fun getCurrentPosition(): Int = audioPlayer.getCurrentPosition()

    fun getDuration(): Int = audioPlayer.getDuration()

    fun release() {
        audioPlayer.release()
        isPrepared = false
        isPlaying = false
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}
