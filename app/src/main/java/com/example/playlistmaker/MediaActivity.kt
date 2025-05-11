package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.IOException

class MediaActivity : AppCompatActivity() {

    private var isTrackPlaying = false
    private var layke = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var handler: Handler
    private lateinit var currentTimeTextView: TextView
    private var currentTrackPosition = 0
    private var isPrepared = false
    private var wasPlayingBeforeConfigChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        handler = Handler(Looper.getMainLooper())
        currentTimeTextView = findViewById(R.id.otzet_vremy)

        // Восстановление состояния при повороте экрана
        if (savedInstanceState != null) {
            isTrackPlaying = savedInstanceState.getBoolean("IS_TRACK_PLAYING", false)
            currentTrackPosition = savedInstanceState.getInt("CURRENT_POSITION", 0)
            wasPlayingBeforeConfigChange = savedInstanceState.getBoolean("WAS_PLAYING", false)
        }

        val backButton = findViewById<ImageButton>(R.id.back_button3)
        backButton.setOnClickListener {
            mediaPlayer.stop()
            finish()
        }

        val trackId = intent.getIntExtra("TRACK_ID", 0)
        val trackName = intent.getStringExtra("TRACK_NAME")
        val artistName = intent.getStringExtra("ARTIST_NAME")
        val artworkUrl = intent.getStringExtra("ARTWORK_URL")?.replaceAfterLast('/', "512x512bb.jpg")
        val collectionName = intent.getStringExtra("COLLECTION_NAME")
        val releaseDate = intent.getStringExtra("RELEASE_DATE")
        val primaryGenre = intent.getStringExtra("PRIMARY_GENRE")
        val country = intent.getStringExtra("COUNTRY")
        val trackTimeMillis = intent.getLongExtra("TRACK_TIME_MILLIS", 0L)
        val previewUrl = intent.getStringExtra("PREVIEW_URL")

        layke = sharedPreferences.getBoolean("TRACK_LIKE_$trackId", false)

        val likeButton = findViewById<ImageView>(R.id.imageView3)
        updateLikeButtonState(likeButton, layke)
        likeButton.setOnClickListener {
            layke = !layke
            updateLikeButtonState(likeButton, layke)
            sharedPreferences.edit().putBoolean("TRACK_LIKE_$trackId", layke).apply()
            Toast.makeText(
                this,
                if (layke) "Лайк поставлен" else "Лайк убран",
                Toast.LENGTH_SHORT
            ).show()
        }

        initializeMediaPlayer(previewUrl)
        setupPlayPauseButton()

        val trackTimeFormatted = formatTrackTime(trackTimeMillis)
        initViews(
            trackName,
            artistName,
            collectionName,
            releaseDate,
            primaryGenre,
            country,
            artworkUrl,
            trackTimeFormatted
        )

        // Показываем текущее время сразу после инициализации
        updateTimeDisplay(currentTrackPosition)
    }

    private fun initializeMediaPlayer(previewUrl: String?) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    isPrepared = true
                    seekTo(currentTrackPosition) // Восстанавливаем позицию

                    if (wasPlayingBeforeConfigChange) {
                        start()
                        isTrackPlaying = true
                        updatePlayPauseButtonState(findViewById(R.id.imageView), true)
                        updateCurrentTime()
                    }
                }
                setOnCompletionListener {
                    handler.post {
                        resetPlaybackState()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    handler.post { handlePlaybackError() }
                    true
                }
            } catch (e: IOException) {
                handlePlaybackError()
            }
        }
    }

    private fun setupPlayPauseButton() {
        val playPauseButton = findViewById<ImageView>(R.id.imageView)
        updatePlayPauseButtonState(playPauseButton, isTrackPlaying)
        playPauseButton.setOnClickListener {
            if (isTrackPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }
    }

    private fun startPlayback() {
        if (!mediaPlayer.isPlaying && isPrepared) {
            try {
                // Если трек закончился - сбрасываем в начало
                if (mediaPlayer.currentPosition >= mediaPlayer.duration - 100) {
                    mediaPlayer.seekTo(0)
                    currentTrackPosition = 0
                }
                mediaPlayer.start()
                isTrackPlaying = true
                updatePlayPauseButtonState(findViewById(R.id.imageView), true)
                updateCurrentTime()
            } catch (e: IllegalStateException) {
                handlePlaybackError()
            }
        }
    }

    private fun pausePlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isTrackPlaying = false
            updatePlayPauseButtonState(findViewById(R.id.imageView), false)
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun resetPlaybackState() {
        mediaPlayer.seekTo(0)
        isTrackPlaying = false
        currentTrackPosition = 0
        updatePlayPauseButtonState(findViewById(R.id.imageView), false)
        updateTimeDisplay(0)
    }

    private fun updateCurrentTime() {
        currentTrackPosition = mediaPlayer.currentPosition
        updateTimeDisplay(currentTrackPosition)

        if (isTrackPlaying) {
            handler.postDelayed({ updateCurrentTime() }, 1000)
        }
    }

    private fun updateTimeDisplay(position: Int) {
        val currentSeconds = position / 1000
        val minutes = currentSeconds / 60
        val seconds = currentSeconds % 60
        currentTimeTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun handlePlaybackError() {
        Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
        resetPlaybackState()
    }

    private fun formatTrackTime(millis: Long): String {
        return if (millis > 0) {
            val minutes = millis / 60000
            val seconds = (millis % 60000) / 1000
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "--:--"
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

    private fun updatePlayPauseButtonState(button: ImageView, isPlaying: Boolean) {
        button.setImageResource(if (isPlaying) R.drawable.buttonpase else R.drawable.button)
    }

    private fun updateLikeButtonState(button: ImageView, isLiked: Boolean) {
        button.setImageResource(if (isLiked) R.drawable.button__4_ else R.drawable.button__3_)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_TRACK_PLAYING", isTrackPlaying)
        outState.putInt("CURRENT_POSITION", currentTrackPosition)
        outState.putBoolean("WAS_PLAYING", isTrackPlaying && isPrepared)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isTrackPlaying = savedInstanceState.getBoolean("IS_TRACK_PLAYING", false)
        currentTrackPosition = savedInstanceState.getInt("CURRENT_POSITION", 0)
        wasPlayingBeforeConfigChange = savedInstanceState.getBoolean("WAS_PLAYING", false)
    }

    override fun onPause() {
        super.onPause()
        if (isTrackPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}