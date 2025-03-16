package com.example.playlistmaker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class TrackAdapter(private val context: Context, private var tracks: List<Track>) :
    RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    // Метод для обновления списка треков
    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        // Инициализация ViewHolder с использованием разметки item_track.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        // Привязка данных к ViewHolder
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    // Внутренний класс ViewHolder
    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image_view)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name_text_view)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name_text_view)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time_text_view)

        fun bind(track: Track) {
            // Установка названия трека
            trackNameTextView.text = track.trackName ?: "Неизвестный трек"

            // Установка имени исполнителя
            artistNameTextView.text = track.artistName ?: "Неизвестный исполнитель"

            // Установка времени трека
            trackTimeTextView.text = track.trackTime

            // Загрузка обложки с использованием Glide
            if (track.artworkUrl100.isNullOrEmpty()) {
                // Если ссылка на обложку отсутствует, показываем заглушку
                artworkImageView.setImageResource(R.drawable.placeholder)
            } else {
                Glide.with(context)
                    .load(track.artworkUrl100)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.placeholder) // Заглушка во время загрузки
                            .error(R.drawable.placeholder) // Заглушка при ошибке загрузки
                            .transform(RoundedCorners(8)) // Скругление углов
                    )
                    .into(artworkImageView)
            }
        }
    }
}