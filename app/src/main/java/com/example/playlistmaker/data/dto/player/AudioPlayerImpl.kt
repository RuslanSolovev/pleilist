package com.example.playlistmaker.data.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.playlistmaker.domain.player.AudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    override fun prepare(url: String, onPrepared: () -> Unit, onError: () -> Unit) {
        release()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync() // ⚠️ Важно: асинхронная подготовка
                setOnPreparedListener {
                    Log.d("AudioPlayer", "MediaPlayer prepared") // Логируем успех
                    onPrepared()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error: what=$what, extra=$extra") // Логируем ошибку
                    onError()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Exception: ${e.message}") // Логируем исключение
            onError()
        }
    }

    override fun start() {
        mediaPlayer?.start()
    }

    override fun pause() {
        mediaPlayer?.pause()
    }


    override fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}