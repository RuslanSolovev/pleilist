package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import android.util.Log
import com.example.playlistmaker.domain.repository.AudioPlayer


class AudioPlayerImpl (
) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    override fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onError: () -> Unit
    ) {
        // Всегда освобождаем старый плеер перед новым
        release()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()

                setOnPreparedListener {
                    Log.d("AudioPlayer", "MediaPlayer prepared")
                    onPrepared()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    onError()
                    true
                }

                // **Важно**: при завершении трека вызываем сохранённый внешний коллбэк
                setOnCompletionListener {
                    Log.d("AudioPlayer", "MediaPlayer completed")
                    onCompletionListener?.invoke()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error preparing MediaPlayer", e)
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
