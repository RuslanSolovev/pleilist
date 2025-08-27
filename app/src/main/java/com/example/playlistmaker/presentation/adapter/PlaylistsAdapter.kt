// package com.example.playlistmaker.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Playlist

// Определяем интерфейс внутри файла адаптера
interface OnPlaylistLongClickListener {
    fun onPlaylistLongClick(playlist: Playlist)
}

class PlaylistsAdapter(
    private val onPlaylistLongClickListener: OnPlaylistLongClickListener? = null
) : ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    companion object {
        private const val TAG = "PlaylistsAdapter"
    }

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val coverImageView: ImageView = view.findViewById(R.id.playlist_cover)
        private val nameTextView: TextView = view.findViewById(R.id.playlist_name)
        private val trackCountTextView: TextView = view.findViewById(R.id.playlist_track_count)

        fun bind(playlist: Playlist, longClickListener: OnPlaylistLongClickListener?) {
            Log.d(TAG, "Binding view holder for playlist: ${playlist.name} (ID: ${playlist.id}) at position ${adapterPosition}")

            // Загрузка обложки с помощью Glide
            if (!playlist.coverImagePath.isNullOrBlank()) {
                Glide.with(coverImageView.context)
                    .load(playlist.coverImagePath)
                    .apply(RequestOptions().centerCrop().transform(RoundedCorners(8)))
                    .placeholder(R.drawable.vector)
                    .error(R.drawable.vector)
                    .into(coverImageView)
            } else {
                coverImageView.setImageResource(R.drawable.vector)
            }

            nameTextView.text = playlist.name

            // Локализация количества треков
            val trackCountText = when (playlist.tracksCount) {
                0 -> itemView.context.getString(R.string.no_tracks)
                1 -> "1 ${itemView.context.getString(R.string.one_track)}"
                2, 3, 4 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.few_tracks)}"
                else -> {
                    val lastDigit = playlist.tracksCount % 10
                    val lastTwoDigits = playlist.tracksCount % 100
                    when {
                        lastTwoDigits in 11..14 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.many_tracks)}"
                        lastDigit == 1 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.one_track)}"
                        lastDigit in 2..4 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.few_tracks)}"
                        else -> "${playlist.tracksCount} ${itemView.context.getString(R.string.many_tracks)}"
                    }
                }
            }
            trackCountTextView.text = trackCountText

            // Установка обработчика долгого нажатия
            if (longClickListener != null) {
                itemView.setOnLongClickListener {
                    longClickListener.onPlaylistLongClick(playlist)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        Log.d(TAG, "onCreateViewHolder called for viewType: $viewType")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        Log.d(TAG, "onBindViewHolder called for position: $position")
        holder.bind(playlist, onPlaylistLongClickListener)
    }

    // Переопределяем submitList для логирования
    override fun submitList(list: List<Playlist>?) {
        Log.d(TAG, "submitList called with ${list?.size ?: 0} items")
        super.submitList(list)
        Log.d(TAG, "submitList completed")
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d(TAG, "getItemCount returning: $count")
        return count
    }
}

class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }
}