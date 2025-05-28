package com.example.playlistmaker.domain.repository

interface AudioPlayer {
    fun prepare(url: String, onPrepared: () -> Unit, onError: () -> Unit)
    fun start()
    fun pause()
    fun seekTo(position: Int)
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun isPlaying(): Boolean
    fun release()
    fun setOnCompletionListener(listener: () -> Unit)
}
