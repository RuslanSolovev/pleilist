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
import com.example.playlistmaker.domain.model.Playlist

class PlaylistBottomSheetAdapter(
    private var playlists: List<Playlist>,
    private val currentTrackId: Int,
    private val onPlaylistClicked: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImageView: ImageView = view.findViewById(R.id.playlistCoverImageView)
        val nameTextView: TextView = view.findViewById(R.id.playlistNameTextView)
        val tracksCountTextView: TextView = view.findViewById(R.id.tracksCountTextView)
        val statusTextView: TextView = view.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_bottom_sheet, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]

        // Загрузка обложки
        if (!playlist.coverImagePath.isNullOrBlank()) {
            Glide.with(holder.coverImageView.context)
                .load(playlist.coverImagePath)
                .apply(RequestOptions().centerCrop().transform(RoundedCorners(8)))
                .placeholder(R.drawable.light_mode)
                .error(R.drawable.light_mode)
                .into(holder.coverImageView)
        } else {
            holder.coverImageView.setImageResource(R.drawable.light_mode)
        }

        holder.nameTextView.text = playlist.name

        // Локализация количества треков
        val trackCountText = when (playlist.tracksCount) {
            0 -> holder.itemView.context.getString(R.string.no_tracks)
            1 -> "1 ${holder.itemView.context.getString(R.string.one_track)}"
            2, 3, 4 -> "${playlist.tracksCount} ${holder.itemView.context.getString(R.string.few_tracks)}"
            else -> "${playlist.tracksCount} ${holder.itemView.context.getString(R.string.many_tracks)}"
        }
        holder.tracksCountTextView.text = trackCountText

        // Проверяем, добавлен ли уже трек в этот плейлист
        val isTrackInPlaylist = playlist.trackIds.contains(currentTrackId)
        if (isTrackInPlaylist) {
            holder.statusTextView.text = "Уже добавлен"
            holder.statusTextView.visibility = View.VISIBLE
        } else {
            holder.statusTextView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onPlaylistClicked(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}