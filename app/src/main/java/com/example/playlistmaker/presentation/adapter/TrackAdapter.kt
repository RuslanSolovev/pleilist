package com.example.playlistmaker.presentation.adapter

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
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Интерфейс для обработки кликов по треку ---
interface OnTrackClickListener {
    fun onItemClick(track: Track)
    fun onItemLongClick(track: Track)
}
// ------------------------------------------------

class TrackAdapter(
    private val context: Context,
    private val lifecycleScope: CoroutineScope,
    private val onTrackClickListener: OnTrackClickListener // <<< Принимаем интерфейс
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var tracks = emptyList<Track>()
    private var lastClickJob: Job? = null
    private val clickDebounceTime = 1000L

    private val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.corner_radius_small)
    private val glideOptions = RequestOptions()
        .placeholder(R.drawable.placeholder_vector)
        .error(R.drawable.placeholder_vector)
        .transform(RoundedCorners(cornerRadius))

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position], onTrackClickListener) // <<< Передаем listener
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artwork: ImageView = itemView.findViewById(R.id.artwork_image_view)
        private val trackName: TextView = itemView.findViewById(R.id.track_name_text_view)
        private val artistName: TextView = itemView.findViewById(R.id.artist_name_text_view)
        private val trackTime: TextView = itemView.findViewById(R.id.track_time_text_view)

        fun bind(track: Track, clickListener: OnTrackClickListener) { // <<< Принимаем listener
            trackName.text = track.trackName ?: context.getString(R.string.unknown_track)
            artistName.text = track.artistName ?: context.getString(R.string.unknown_artist)
            trackTime.text = track.trackTime ?: ""

            loadArtwork(track.artworkUrl100)

            // --- Обработчик обычного клика с debounce ---
            itemView.setOnClickListener {
                lastClickJob?.cancel()
                lastClickJob = lifecycleScope.launch {
                    delay(clickDebounceTime)
                    clickListener.onItemClick(track) // <<< Вызываем метод listener'а
                }
            }
            // ---------------------------------------------

            // --- Обработчик долгого клика ---
            itemView.setOnLongClickListener {
                clickListener.onItemLongClick(track) // <<< Вызываем метод listener'а
                true // Возвращаем true, чтобы показать, что событие обработано
            }
            // ---------------------------------
        }

        private fun loadArtwork(url: String?) {
            if (url.isNullOrEmpty()) {
                artwork.setImageResource(R.drawable.placeholder_vector)
            } else {
                Glide.with(context)
                    .load(url)
                    .apply(glideOptions)
                    .into(artwork)
            }
        }
    }
}