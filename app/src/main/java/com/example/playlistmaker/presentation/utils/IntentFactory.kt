package com.example.playlistmaker.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri

class IntentFactory(private val context: Context) {
    fun createEmailIntent(email: String, subject: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
    }

    fun createShareIntent(text: String): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
    }

    fun createViewIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }
}