package com.example.playlistmaker.presentation.activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R

import com.example.playlistmaker.domain.util.TimeFormatter
import com.example.playlistmaker.presentation.viewmodel.MediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MediaActivity : AppCompatActivity() {

    private lateinit var viewModel: MediaViewModel

    private lateinit var currentTimeTextView: TextView
    private lateinit var playPauseButton: ImageView
    private lateinit var likeButton: ImageView

    private var trackId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        // Получение ViewModel через Hilt
        viewModel = ViewModelProvider(this)[MediaViewModel::class.java]

        // Инициализация View
        currentTimeTextView = findViewById(R.id.otzet_vremy)
        playPauseButton = findViewById(R.id.imageView)
        likeButton = findViewById(R.id.imageView3)

        trackId = intent.getIntExtra("TRACK_ID", 0)
        val trackName = intent.getStringExtra("TRACK_NAME")
        val artistName = intent.getStringExtra("ARTIST_NAME")
        val artworkUrl = intent.getStringExtra("ARTWORK_URL")?.replaceAfterLast('/', "512x512bb.jpg")
        val collectionName = intent.getStringExtra("COLLECTION_NAME")
        val releaseDate = intent.getStringExtra("RELEASE_DATE")
        val primaryGenre = intent.getStringExtra("PRIMARY_GENRE")
        val country = intent.getStringExtra("COUNTRY")
        val trackTimeMillis = intent.getLongExtra("TRACK_TIME_MILLIS", 0L)
        val previewUrl = intent.getStringExtra("PREVIEW_URL")
        if (previewUrl != null) {
            viewModel.preparePlayer(previewUrl)
        }

        // Передача данных в ViewModel
        viewModel.setTrackId(trackId)

        // Подписка на обновления времени
        lifecycleScope.launch {
            viewModel.currentTime.collectLatest { time ->
                currentTimeTextView.text = time
            }
        }

        // Подписка на состояние воспроизведения
        viewModel.playState.observe(this) { isPlaying ->
            playPauseButton.setImageResource(if (isPlaying) R.drawable.buttonpase else R.drawable.button)
        }

        // Подписка на лайки
        viewModel.isLiked.observe(this) { liked ->
            likeButton.setImageResource(if (liked) R.drawable.button__4_ else R.drawable.button__3_)
        }

        // Инициализация UI
        initViews(
            trackName,
            artistName,
            collectionName,
            releaseDate,
            primaryGenre,
            country,
            artworkUrl,
            TimeFormatter.formatTrackTime(trackTimeMillis)
        )

        // Обработчики событий
        playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        likeButton.setOnClickListener {
            viewModel.toggleLike()
        }

        val backButton = findViewById<ImageButton>(R.id.back_button3)
        backButton.setOnClickListener {
            viewModel.release()
            finish()
        }
    }

    private fun initViews(
        trackName: String?,
        artistName: String?,
        collectionName: String?,
        releaseDate: String?,
        primaryGenre: String?,
        country: String?,
        artworkUrl: String?,
        trackTimeFormatted: String
    ) {
        findViewById<TextView>(R.id.pesny_nazvanie).text = trackName ?: "Неизвестный трек"
        findViewById<TextView>(R.id.nazvanie_gruppa).text = artistName ?: "Неизвестный исполнитель"
        findViewById<TextView>(R.id.albom2).text = collectionName ?: "Неизвестен"
        findViewById<TextView>(R.id.god2).text = releaseDate?.take(4) ?: "Год неизвестен"
        findViewById<TextView>(R.id.janr2).text = primaryGenre ?: "Неизвестен"
        findViewById<TextView>(R.id.strana2).text = country ?: "Неизвестна"
        findViewById<TextView>(R.id.dlitelnost2).text = trackTimeFormatted

        val artworkView = findViewById<ImageView>(R.id.pleer_image_view)
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

    override fun onPause() {
        super.onPause()
        if (viewModel.playState.value == true) {
            viewModel.togglePlayPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}