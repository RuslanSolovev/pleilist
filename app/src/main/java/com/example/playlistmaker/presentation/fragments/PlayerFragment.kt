package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.util.TimeFormatter
import com.example.playlistmaker.presentation.viewmodel.MediaViewModel
import com.example.playlistmaker.presentation.player.PlayerUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {
    private val viewModel: MediaViewModel by viewModel()
    private lateinit var currentTimeTextView: TextView
    private lateinit var playPauseButton: ImageView
    private lateinit var likeButton: ImageView
    private var trackId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentTimeTextView = view.findViewById(R.id.otzet_vremy)
        playPauseButton = view.findViewById(R.id.imageView)
        likeButton = view.findViewById(R.id.imageView3)

        trackId = arguments?.getInt("TRACK_ID", 0) ?: 0
        val trackName = arguments?.getString("TRACK_NAME")
        val artistName = arguments?.getString("ARTIST_NAME")
        val artworkUrl = arguments?.getString("ARTWORK_URL")?.replaceAfterLast('/', "512x512bb.jpg")
        val collectionName = arguments?.getString("COLLECTION_NAME")
        val releaseDate = arguments?.getString("RELEASE_DATE")
        val primaryGenre = arguments?.getString("PRIMARY_GENRE")
        val country = arguments?.getString("COUNTRY")
        val trackTimeMillis = arguments?.getLong("TRACK_TIME_MILLIS", 0L)
        val previewUrl = arguments?.getString("PREVIEW_URL")

        viewModel.setTrackId(trackId)
        previewUrl?.let { viewModel.preparePlayer(it) }

        initViews(
            view,
            trackName,
            artistName,
            collectionName,
            releaseDate,
            primaryGenre,
            country,
            artworkUrl,
            TimeFormatter.formatTrackTime(trackTimeMillis ?: 0L)
        )

        setupObservers()
        setupClickListeners(view)
    }

    private fun initViews(
        view: View,
        trackName: String?,
        artistName: String?,
        collectionName: String?,
        releaseDate: String?,
        primaryGenre: String?,
        country: String?,
        artworkUrl: String?,
        trackTimeFormatted: String
    ) {
        view.findViewById<TextView>(R.id.pesny_nazvanie).text = trackName ?: "Неизвестный трек"
        view.findViewById<TextView>(R.id.nazvanie_gruppa).text = artistName ?: "Неизвестный исполнитель"
        view.findViewById<TextView>(R.id.albom2).text = collectionName ?: "Неизвестен"
        view.findViewById<TextView>(R.id.god2).text = releaseDate?.take(4) ?: "Год неизвестен"
        view.findViewById<TextView>(R.id.janr2).text = primaryGenre ?: "Неизвестен"
        view.findViewById<TextView>(R.id.strana2).text = country ?: "Неизвестна"
        view.findViewById<TextView>(R.id.dlitelnost2).text = trackTimeFormatted

        val artworkView = view.findViewById<ImageView>(R.id.pleer_image_view)
        if (!artworkUrl.isNullOrEmpty()) {
            val radiusInPx = resources.getDimensionPixelSize(R.dimen.corner_radius_big)
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .transform(RoundedCorners(radiusInPx))
                .into(artworkView)
        } else {
            artworkView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is PlayerUiState.Content -> {
                        currentTimeTextView.text = state.currentTime
                        playPauseButton.setImageResource(
                            if (state.isPlaying) R.drawable.buttonpase else R.drawable.button
                        )
                        likeButton.setImageResource(
                            if (state.isLiked) R.drawable.button__4_ else R.drawable.button__3_
                        )
                    }
                    is PlayerUiState.Error -> showError(state.message)
                    PlayerUiState.Loading -> Unit
                }
            }
        }
    }

    private fun setupClickListeners(view: View) {
        playPauseButton.setOnClickListener { viewModel.togglePlayPause() }
        likeButton.setOnClickListener { viewModel.toggleLike() }
        view.findViewById<ImageButton>(R.id.back_button3).setOnClickListener {
            viewModel.release()
            parentFragmentManager.popBackStack()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if ((viewModel.uiState.value as? PlayerUiState.Content)?.isPlaying == true) {
            viewModel.togglePlayPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}