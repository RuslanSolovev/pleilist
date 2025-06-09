package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.interactor.AudioPlayerInteractor
import com.example.playlistmaker.domain.util.TimeFormatter

class MediaPlayerInteractor(
    private val audioPlayerInteractor: AudioPlayerInteractor,
    private val timeFormatter: TimeFormatter
) {
    private var isPlaying = false

    fun prepare(url: String, onPrepared: () -> Unit, onError: (String) -> Unit) {
        audioPlayerInteractor.updateTrackUrl(url)
        audioPlayerInteractor.prepare(
            onPrepared = {
                isPlaying = false
                onPrepared()
            },
            onError = { onError("Произошла ошибка при подготовке аудио") }
        )
    }

    fun play() {
        audioPlayerInteractor.play()
        isPlaying = true
    }

    fun pause() {
        audioPlayerInteractor.pause()
        isPlaying = false
    }

    fun togglePlayPause() {
        if (isPlaying) pause() else play()
    }

    fun getCurrentPosition(): Int = audioPlayerInteractor.getCurrentPosition()
    fun getFormattedCurrentPosition(): String = timeFormatter.formatTrackTime(getCurrentPosition().toLong())
    fun getDuration(): Int = audioPlayerInteractor.getDuration()
    fun seekTo(position: Int) = audioPlayerInteractor.seekTo(position)
    fun release() = audioPlayerInteractor.release()

    fun setOnCompletionListener(listener: () -> Unit) {
        audioPlayerInteractor.setOnCompletionListener {
            isPlaying = false
            listener()
        }
    }
}