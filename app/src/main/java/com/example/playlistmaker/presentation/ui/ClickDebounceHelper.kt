package com.example.playlistmaker.presentation.ui

class ClickDebounceHelper(private val debounceMillis: Long = 1000L) {
    private var lastClickTime = 0L


    fun canClick(): Boolean {
        val now = System.currentTimeMillis()
        return if (now - lastClickTime >= debounceMillis) {
            lastClickTime = now
            true
        } else {
            false
        }
    }
}
