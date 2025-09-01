import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Playlist

class PlaylistBottomSheetAdapter(
    private var playlists: List<Playlist>,
    private val currentTrackId: Int,
    private val onPlaylistClicked: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverContainer: FrameLayout = view.findViewById(R.id.coverContainer)
        val coverImageView: ImageView = view.findViewById(R.id.playlistCoverImageView)
        val nameTextView: TextView = view.findViewById(R.id.playlistNameTextView)
        val tracksCountTextView: TextView = view.findViewById(R.id.tracksCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_bottom_sheet, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]

        // Применяем закругление к контейнеру
        holder.coverContainer.clipToOutline = true
        holder.coverContainer.outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: android.view.View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 2f * view.resources.displayMetrics.density)
            }
        }

        // Загрузка обложки
        if (!playlist.coverImagePath.isNullOrBlank()) {
            Glide.with(holder.coverImageView.context)
                .load(playlist.coverImagePath)
                .centerCrop()
                .placeholder(R.drawable.vector)
                .error(R.drawable.vector)
                .into(holder.coverImageView)
        } else {
            holder.coverImageView.setImageResource(R.drawable.vector)
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