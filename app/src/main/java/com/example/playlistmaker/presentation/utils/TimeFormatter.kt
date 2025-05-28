package com.example.playlistmaker.presentation.utils

object TimeFormatter {
    fun formatTrackTime(millis: Long): String {
        return if (millis > 0) {
            val minutes = millis / 60000
            val seconds = (millis % 60000) / 1000
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "--:--"
        }
    }

    fun formatPlaybackTime(position: Int): String {
        val currentSeconds = position / 1000
        val minutes = currentSeconds / 60
        val seconds = currentSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}