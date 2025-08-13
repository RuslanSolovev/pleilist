package com.example.playlistmaker.presentation.ui

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.playlistmaker.R

class PlaceholderRenderer(
    private val context: Context,
    private val placeholderRoot: View,
    private val imageView: ImageView,
    private val textView: TextView,
    private val retryButton: View
) {

    fun showLoading() {
        placeholderRoot.isVisible = true
        imageView.isVisible = false
        textView.isVisible = false
        retryButton.isVisible = false
    }

    fun showNoResults() {
        placeholderRoot.isVisible = true
        imageView.isVisible = true
        textView.isVisible = true
        retryButton.isVisible = false

        imageView.setImageResource(R.drawable.light_mode)
        textView.text = context.getString(R.string.no_results)
    }

    fun showError() {
        placeholderRoot.isVisible = true
        imageView.isVisible = true
        textView.isVisible = true
        retryButton.isVisible = true

        imageView.setImageResource(R.drawable.nointer)
        textView.text = context.getString(R.string.server_error)
    }

    fun hidePlaceholder() {
        placeholderRoot.isVisible = false
    }
}
