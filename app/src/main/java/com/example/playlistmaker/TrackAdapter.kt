import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.Track

class TrackAdapter(private val context: Context, private var tracks: List<Track>) :
    RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image_view)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name_text_view)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name_text_view)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time_text_view)

        fun bind(track: Track) {
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName
            trackTimeTextView.text = track.trackTime

            Glide.with(context)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.placeholder)
                .transform(RoundedCorners(2))
                .error(R.drawable.nastroi)
                .centerCrop()
                .into(artworkImageView)
        }
    }
}