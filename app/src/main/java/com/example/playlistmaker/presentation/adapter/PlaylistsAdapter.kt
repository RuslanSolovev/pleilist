package com.example.playlistmaker.presentation.adapter

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



interface OnPlaylistClickListener {

    fun onPlaylistClick(playlist: Playlist)
}


interface OnPlaylistLongClickListener {

    fun onPlaylistLongClick(playlist: Playlist)
}
// ---------------------------------------


class PlaylistsAdapter(
    private val onPlaylistClickListener: OnPlaylistClickListener? = null,
    private val onPlaylistLongClickListener: OnPlaylistLongClickListener? = null
) : ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    companion object {
        private const val TAG = "PlaylistsAdapter"
    }


    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val coverImageView: ImageView = view.findViewById(R.id.playlist_cover) // Убедитесь, что ID совпадает с item_playlist.xml
        private val nameTextView: TextView = view.findViewById(R.id.playlist_name)
        private val trackCountTextView: TextView = view.findViewById(R.id.playlist_track_count)

        fun bind(playlist: Playlist, clickListener: OnPlaylistClickListener?, longClickListener: OnPlaylistLongClickListener?) {
            Log.d(TAG, "Binding view holder for playlist: ${playlist.name} (ID: ${playlist.id}) at position ${adapterPosition}")

            // --- Загрузка обложки ---
            if (!playlist.coverImagePath.isNullOrBlank()) {
                Glide.with(coverImageView.context)
                    .load(playlist.coverImagePath)
                    .apply(RequestOptions().centerCrop().transform(RoundedCorners(8))) // Закругленные углы 8dp
                    .placeholder(R.drawable.placeholder_vector) // Убедитесь, что drawable существует
                    .error(R.drawable.placeholder_vector) // Убедитесь, что drawable существует
                    .into(coverImageView)
            } else {
                coverImageView.setImageResource(R.drawable.placeholder_vector)
            }

            nameTextView.text = playlist.name

            val trackCountText = when (playlist.tracksCount) {
                0 -> itemView.context.getString(R.string.no_tracks)
                1 -> "1 ${itemView.context.getString(R.string.one_track)}"
                2, 3, 4 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.few_tracks)}"
                else -> {

                    val lastDigit = playlist.tracksCount % 10
                    val lastTwoDigits = playlist.tracksCount % 100
                    when {

                        lastTwoDigits in 11..14 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.many_tracks)}"

                        lastDigit == 1 -> "1 ${itemView.context.getString(R.string.one_track)}"

                        lastDigit in 2..4 -> "${playlist.tracksCount} ${itemView.context.getString(R.string.few_tracks)}"

                        else -> "${playlist.tracksCount} ${itemView.context.getString(R.string.many_tracks)}"
                    }
                }
            }
            trackCountTextView.text = trackCountText
            Log.d(TAG, "Formatted track count for '${playlist.name}': $trackCountText")

            itemView.setOnClickListener {
                clickListener?.onPlaylistClick(playlist)
            }


            itemView.setOnLongClickListener {
                longClickListener?.onPlaylistLongClick(playlist)
                // Возвращаем true, чтобы показать, что событие обработано
                true
            }
            // ------------------------------------
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        Log.d(TAG, "onCreateViewHolder called for viewType: $viewType")
        // Убедитесь, что R.layout.item_playlist существует и корректен
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }


    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        Log.d(TAG, "onBindViewHolder called for position: $position")
        // Передаем слушатели в метод bind конкретного ViewHolder
        holder.bind(playlist, onPlaylistClickListener, onPlaylistLongClickListener)
    }

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