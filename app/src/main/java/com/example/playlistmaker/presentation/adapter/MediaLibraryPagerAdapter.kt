// MediaLibraryPagerAdapter.kt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.example.playlistmaker.presentation.ui.PlaylistsFragment

class MediaLibraryPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val playlistItemClickListener: PlaylistsFragment.OnPlaylistItemClickListener? = null // <-- Добавлен третий аргумент
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoritesFragment()
            1 -> {
                val playlistsFragment = PlaylistsFragment()
                // Передаем listener в PlaylistsFragment, если он не null
                // Предполагается, что в PlaylistsFragment есть свойство playlistItemClickListener
                if (playlistItemClickListener != null) {
                    playlistsFragment.playlistItemClickListener = playlistItemClickListener
                }
                playlistsFragment
            }
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}