package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import android.util.Log
import com.example.playlistmaker.domain.repository.AudioPlayer

class AudioPlayerImpl(
    private val mediaPlayer: MediaPlayer
) : AudioPlayer {

    private var onCompletionListener: (() -> Unit)? = null

    override fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onError: () -> Unit
    ) {
        release()

        try {
            mediaPlayer.apply {
                reset()
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
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun getDuration(): Int {
        return mediaPlayer.duration
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun release() {
        mediaPlayer.reset()
        onCompletionListener = null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
        mediaPlayer.setOnCompletionListener {
            listener()
        }
    }
}