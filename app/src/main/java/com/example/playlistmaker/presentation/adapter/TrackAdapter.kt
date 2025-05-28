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

class TrackAdapter(
    private val context: Context,
    private var tracks: List<Track>,
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    // Разные радиусы для разных случаев
    private val smallCornerRadius = context.resources.getDimensionPixelSize(R.dimen.corner_radius_small) // 2dp
    private val bigCornerRadius = context.resources.getDimensionPixelSize(R.dimen.corner_radius_big)   // 8dp

    // Оптимизированные настройки Glide
    private val glideOptionsSmall = RequestOptions()
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.placeholder)
        .transform(RoundedCorners(smallCornerRadius))

    private val glideOptionsBig = RequestOptions()
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.placeholder)
        .transform(RoundedCorners(bigCornerRadius))

    // Флаг для определения типа отображения
    var isLargeArtworkMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)

        holder.itemView.setOnClickListener {
            it.isPressed = true
            it.postDelayed({ it.isPressed = false }, 200)
            onItemClick(track)
        }
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image_view)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name_text_view)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name_text_view)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time_text_view)

        fun bind(track: Track) {
            trackNameTextView.text = track.trackName ?: context.getString(R.string.unknown_track)
            artistNameTextView.text = track.artistName ?: context.getString(R.string.unknown_artist)
            trackTimeTextView.text = track.trackTime ?: ""

            loadArtwork(track)
        }

        private fun loadArtwork(track: Track) {
            if (track.artworkUrl100.isNullOrEmpty()) {
                artworkImageView.setImageResource(R.drawable.placeholder)
            } else {
                // Выбираем настройки в зависимости от режима отображения
                val options = if (isLargeArtworkMode) glideOptionsBig else glideOptionsSmall

                Glide.with(context)
                    .load(track.artworkUrl100)
                    .apply(options)
                    .into(artworkImageView)
            }
        }
    }
}