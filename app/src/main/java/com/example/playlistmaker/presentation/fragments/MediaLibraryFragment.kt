package com.example.playlistmaker.presentation.fragments

import MediaLibraryPagerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaLibraryBinding
import com.example.playlistmaker.presentation.ui.PlaylistsFragment
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment : Fragment(), PlaylistsFragment.OnPlaylistItemClickListener {

    private var _binding: ActivityMediaLibraryBinding? = null
    private val binding get() = _binding!!

    private val TAG = "MediaLibraryFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = ActivityMediaLibraryBinding.inflate(inflater, container, false)
        Log.d(TAG, "Binding inflated")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        setupViewPager()
    }

    private fun setupViewPager() {
        Log.d(TAG, "setupViewPager called")
        // Передаем 'this' как listener для обработки кликов по плейлистам
        val adapter = MediaLibraryPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, this)
        binding.viewPager.adapter = adapter
        Log.d(TAG, "ViewPager adapter set")

        // Связываем TabLayout с ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tab_favorites)
                1 -> tab.text = getString(R.string.tab_playlists)
                else -> tab.text = "Tab ${position + 1}"
            }
        }.attach()
        Log.d(TAG, "TabLayoutMediator attached")
    }

    // --- Реализация интерфейса PlaylistsFragment.OnPlaylistItemClickListener ---
    /**
     * Вызывается из PlaylistsFragment при клике на плейлист.
     * Выполняет фактическую навигацию к PlaylistDetailsFragment.
     * @param playlistId Идентификатор выбранного плейлиста.
     */
    override fun onPlaylistItemClicked(playlistId: Long) {
        Log.d(TAG, "onPlaylistItemClicked: Navigating to details for playlist ID: $playlistId")
        try {
            // Создаем Bundle с аргументом
            val bundle = Bundle().apply {
                putLong("playlistId", playlistId)
            }
            // Выполняем навигацию из MediaLibraryFragment
            // Убедитесь, что в nav_graph.xml есть действие
            // action_mediaLibraryFragment_to_playlistDetailsFragment
            findNavController().navigate(
                R.id.action_mediaLibraryFragment_to_playlistDetailsFragment,
                bundle
            )
            Log.d(TAG, "Navigation to playlist details initiated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Navigation to playlist details failed", e)
            Toast.makeText(
                requireContext(),
                "Ошибка навигации к деталям плейлиста",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    // -----------------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
}