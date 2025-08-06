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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {

    private val viewModel: MediaViewModel by viewModel()

    private lateinit var currentTimeTextView: TextView
    private lateinit var playPauseButton: ImageView
    private lateinit var likeButton: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var albumTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var genreTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var artworkImageView: ImageView

    private var trackId: Int = 0

    companion object {
        fun newInstance(
            trackId: Int,
            trackName: String?,
            artistName: String?,
            artworkUrl: String?,
            collectionName: String?,
            releaseDate: String?,
            primaryGenre: String?,
            country: String?,
            trackTimeMillis: Long?,
            previewUrl: String?
        ): PlayerFragment {
            return PlayerFragment().apply {
                arguments = Bundle().apply {
                    putInt("TRACK_ID", trackId)
                    putString("TRACK_NAME", trackName)
                    putString("ARTIST_NAME", artistName)
                    putString("ARTWORK_URL", artworkUrl)
                    putString("COLLECTION_NAME", collectionName)
                    putString("RELEASE_DATE", releaseDate)
                    putString("PRIMARY_GENRE", primaryGenre)
                    putString("COUNTRY", country)
                    trackTimeMillis?.let { putLong("TRACK_TIME_MILLIS", it) }
                    putString("PREVIEW_URL", previewUrl)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupObservers()
        setupClickListeners(view)
        loadTrackData()
    }

    private fun initViews(view: View) {
        currentTimeTextView = view.findViewById(R.id.otzet_vremy)
        playPauseButton = view.findViewById(R.id.imageView)
        likeButton = view.findViewById(R.id.imageView3)
        trackNameTextView = view.findViewById(R.id.pesny_nazvanie)
        artistNameTextView = view.findViewById(R.id.nazvanie_gruppa)
        albumTextView = view.findViewById(R.id.albom2)
        yearTextView = view.findViewById(R.id.god2)
        genreTextView = view.findViewById(R.id.janr2)
        countryTextView = view.findViewById(R.id.strana2)
        durationTextView = view.findViewById(R.id.dlitelnost2)
        artworkImageView = view.findViewById(R.id.pleer_image_view)
    }

    private fun loadTrackData() {
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

        trackNameTextView.text = trackName ?: getString(R.string.unknown_track)
        artistNameTextView.text = artistName ?: getString(R.string.unknown_artist)
        albumTextView.text = collectionName ?: getString(R.string.unknown_album)
        yearTextView.text = releaseDate?.take(4) ?: getString(R.string.unknown_year)
        genreTextView.text = primaryGenre ?: getString(R.string.unknown_genre)
        countryTextView.text = country ?: getString(R.string.unknown_country)
        durationTextView.text = TimeFormatter.formatTrackTime(trackTimeMillis ?: 0L)

        if (!artworkUrl.isNullOrEmpty()) {
            val radiusInPx = resources.getDimensionPixelSize(R.dimen.corner_radius_big)
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .transform(RoundedCorners(radiusInPx))
                .into(artworkImageView)
        } else {
            artworkImageView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                currentTimeTextView.text = state.currentTime
                playPauseButton.setImageResource(
                    if (state.isPlaying) R.drawable.buttonpase else R.drawable.button
                )
                likeButton.setImageResource(
                    if (state.isLiked) R.drawable.button__4_ else R.drawable.button__3_
                )

                state.error?.let { error ->
                    showError(error)
                }
            }
        }
    }

    private fun setupClickListeners(view: View) {
        playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        likeButton.setOnClickListener {
            viewModel.toggleLike()
        }

        view.findViewById<ImageButton>(R.id.back_button3).setOnClickListener {
            viewModel.releasePlayer()
            parentFragmentManager.popBackStack()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.uiState.value.isPlaying) {
            viewModel.togglePlayPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }
}