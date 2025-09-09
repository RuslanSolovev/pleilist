// package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapter.OnTrackClickListener
import com.example.playlistmaker.presentation.adapter.TrackAdapter

import com.example.playlistmaker.presentation.viewmodel.FavoritesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // --- Создаем объект, реализующий OnTrackClickListener ---
        val trackClickListener = object : OnTrackClickListener {
            override fun onItemClick(track: Track) {
                handleTrackClick(track)
            }

            override fun onItemLongClick(track: Track) {

                Toast.makeText(requireContext(), "Долгий клик: ${track.trackName}", Toast.LENGTH_SHORT).show()

            }
        }
        // -------------------------------------------------------------

        // --- Передаем объект-слушатель в конструктор адаптера ---
        adapter = TrackAdapter(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            onTrackClickListener = trackClickListener // <<< Передаем OnTrackClickListener
        )
        // ---------------------------------------------------------

        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                showEmptyState()
            } else {
                showFavoritesList(tracks)
            }
        }
    }

    private fun showEmptyState() {
        binding.recyclerViewFavorites.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
    }

    private fun showFavoritesList(tracks: List<Track>) {
        binding.emptyStateView.visibility = View.GONE
        binding.recyclerViewFavorites.visibility = View.VISIBLE
        adapter.updateTracks(tracks)
    }

    private fun handleTrackClick(track: Track) {
        // Создаем Bundle с аргументами для PlayerFragment
        val bundle = Bundle().apply {
            putInt("TRACK_ID", track.trackId)
            putString("TRACK_NAME", track.trackName)
            putString("ARTIST_NAME", track.artistName)
            putString("ARTWORK_URL", track.artworkUrl100)
            putString("COLLECTION_NAME", track.collectionName)
            putString("RELEASE_DATE", track.releaseDate)
            putString("PRIMARY_GENRE", track.primaryGenreName)
            putString("COUNTRY", track.country)
            putLong("TRACK_TIME_MILLIS", track.trackTimeMillis ?: 0L)
            putString("PREVIEW_URL", track.previewUrl)
        }

        // Переходим на PlayerFragment, передавая аргументы
        try {
            findNavController().navigate(R.id.playerFragment, bundle)
        } catch (e: Exception) {
            // Обработка ошибки навигации
            Toast.makeText(requireContext(), "Ошибка перехода к плееру", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}