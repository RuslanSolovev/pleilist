package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MediaActivity : AppCompatActivity() {

    private var isTrackPlaying = false // Состояние кнопки "Play/Pause"
    private var layke = false // Состояние лайка
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Восстановление состояния из savedInstanceState
        if (savedInstanceState != null) {
            isTrackPlaying = savedInstanceState.getBoolean("IS_TRACK_PLAYING", false)
        }

        // Находим кнопку "Назад"
        val backButton = findViewById<ImageButton>(R.id.back_button3)
        backButton.setOnClickListener {
            finish() // Закрыть активити и вернуться назад
        }


        // Извлекаем данные из Intent
        val trackId = intent.getIntExtra("TRACK_ID", 0)
        val trackName = intent.getStringExtra("TRACK_NAME")
        val artistName = intent.getStringExtra("ARTIST_NAME")
        val artworkUrl = intent.getStringExtra("ARTWORK_URL")?.replaceAfterLast('/', "512x512bb.jpg")
        val collectionName = intent.getStringExtra("COLLECTION_NAME")
        val releaseDate = intent.getStringExtra("RELEASE_DATE")
        val primaryGenre = intent.getStringExtra("PRIMARY_GENRE")
        val country = intent.getStringExtra("COUNTRY")
        val trackTimeMillis = intent.getLongExtra("TRACK_TIME_MILLIS", 0L)

        // Восстанавливаем состояние лайка из SharedPreferences
        layke = sharedPreferences.getBoolean("TRACK_LIKE_$trackId", false)

        // Кнопка "Лайк"
        val likeButton = findViewById<ImageView>(R.id.imageView3)
        updateLikeButtonState(likeButton, layke) // Устанавливаем начальное состояние кнопки "Лайк"
        likeButton.setOnClickListener {
            layke = !layke
            updateLikeButtonState(likeButton, layke)
            sharedPreferences.edit().putBoolean("TRACK_LIKE_$trackId", layke).apply()
            if (layke) {
                Toast.makeText(this, "Лайк поставлен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Лайк убран", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка "Play/Pause"
        val playPauseButton = findViewById<ImageView>(R.id.imageView)
        updatePlayPauseButtonState(playPauseButton, isTrackPlaying) // Устанавливаем начальное состояние кнопки
        playPauseButton.setOnClickListener {
            isTrackPlaying = !isTrackPlaying
            updatePlayPauseButtonState(playPauseButton, isTrackPlaying)
            if (isTrackPlaying) {
                Toast.makeText(this, "Трек включен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Трек остановлен", Toast.LENGTH_SHORT).show()
            }
        }

        // Преобразуем длительность в формат "минуты:секунды"
        val trackTimeFormatted = if (trackTimeMillis > 0) {
            val minutes = trackTimeMillis / 60000
            val seconds = (trackTimeMillis % 60000) / 1000
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "--:--"
        }

        // Находим элементы интерфейса
        val trackTitle = findViewById<TextView>(R.id.pesny_nazvanie)
        val artistGroup = findViewById<TextView>(R.id.nazvanie_gruppa)
        val albumInfo = findViewById<TextView>(R.id.albom2)
        val yearInfo = findViewById<TextView>(R.id.god2)
        val genreInfo = findViewById<TextView>(R.id.janr2)
        val countryInfo = findViewById<TextView>(R.id.strana2)
        val artworkView = findViewById<ImageView>(R.id.pleer_image_view)
        val durationTextView = findViewById<TextView>(R.id.dlitelnost2)

        // Устанавливаем данные
        trackTitle.text = trackName ?: "Неизвестный трек"
        artistGroup.text = artistName ?: "Неизвестный исполнитель"
        albumInfo.text = collectionName ?: "Неизвестен"
        yearInfo.text = releaseDate?.take(4) ?: "Год неизвестен"
        genreInfo.text = primaryGenre ?: "Неизвестен"
        countryInfo.text = country ?: "Неизвестна"
        durationTextView.text = trackTimeFormatted

        // Загружаем изображение обложки с помощью Glide.
        if (!artworkUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(artworkView)
        } else {
            artworkView.setImageResource(R.drawable.placeholder)
        }
    }

    // Метод для обновления состояния кнопки "Play/Pause"
    private fun updatePlayPauseButtonState(button: ImageView, isPlaying: Boolean) {
        button.setImageResource(if (isPlaying) R.drawable.buttonpase else R.drawable.button)
    }

    // Метод для обновления состояния кнопки "Лайк"
    private fun updateLikeButtonState(button: ImageView, isLiked: Boolean) {
        button.setImageResource(if (isLiked) R.drawable.button__4_ else R.drawable.button__3_)
    }

    // Сохранение состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_TRACK_PLAYING", isTrackPlaying)
    }

    // Восстановление состояния
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isTrackPlaying = savedInstanceState.getBoolean("IS_TRACK_PLAYING", false)
    }
}