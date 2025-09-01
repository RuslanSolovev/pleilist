package com.example.playlistmaker.presentation.fragments

import PlaylistBottomSheetAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaBinding
import com.example.playlistmaker.databinding.BottomSheetAddToPlaylistBinding
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.util.TimeFormatter

import com.example.playlistmaker.presentation.viewmodel.AddToPlaylistResult
import com.example.playlistmaker.presentation.viewmodel.MediaViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {

    private var _binding: ActivityMediaBinding? = null
    // Используем пользовательский геттер для безопасного доступа
    private val binding get() = _binding!!
    private val viewModel: MediaViewModel by viewModel()

    // Переменная для хранения ссылки на BottomSheetDialog
    private var bottomSheetDialog: BottomSheetDialog? = null

    private val TAG = "PlayerFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = ActivityMediaBinding.inflate(inflater, container, false)
        Log.d(TAG, "Binding inflated")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        setupObservers()
        setupClickListeners()
        loadTrackData()

        // Загружаем плейлисты при открытии фрагмента
        viewModel.refreshPlaylists()
    }

    private fun loadTrackData() {
        Log.d(TAG, "loadTrackData called")
        val trackId = arguments?.getInt("TRACK_ID", 0) ?: 0
        val trackName = arguments?.getString("TRACK_NAME")
        val artistName = arguments?.getString("ARTIST_NAME")
        val artworkUrl = arguments?.getString("ARTWORK_URL")?.replaceAfterLast('/', "512x512bb.jpg")
        val collectionName = arguments?.getString("COLLECTION_NAME")
        val releaseDate = arguments?.getString("RELEASE_DATE")
        val primaryGenre = arguments?.getString("PRIMARY_GENRE")
        val country = arguments?.getString("COUNTRY")
        val trackTimeMillis = arguments?.getLong("TRACK_TIME_MILLIS", 0L)
        val previewUrl = arguments?.getString("PREVIEW_URL")

        Log.d(
            TAG,
            "Track data received - ID: $trackId, Name: $trackName, Artist: $artistName, Preview URL: $previewUrl"
        )

        viewModel.setTrackData(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            artworkUrl = artworkUrl,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenre = primaryGenre,
            country = country,
            trackTimeMillis = trackTimeMillis,
            previewUrl = previewUrl
        )

        previewUrl?.let {
            Log.d(TAG, "Preparing player with preview URL")
            viewModel.preparePlayer(it)
        }

        // Обновление UI данными трека
        binding.pesnyNazvanie.text = trackName ?: getString(R.string.unknown_track)
        binding.nazvanieGruppa.text = artistName ?: getString(R.string.unknown_artist)
        binding.albom2.text = collectionName ?: getString(R.string.unknown_album)
        binding.god2.text = releaseDate?.take(4) ?: getString(R.string.unknown_year)
        binding.janr2.text = primaryGenre ?: getString(R.string.unknown_genre)
        binding.strana2.text = country ?: getString(R.string.unknown_country)
        binding.dlitelnost2.text = TimeFormatter.formatTrackTime(trackTimeMillis ?: 0L)

        if (!artworkUrl.isNullOrEmpty()) {
            val radiusInPx = resources.getDimensionPixelSize(R.dimen.corner_radius_big)
            Log.d(TAG, "Loading artwork with radius: $radiusInPx")
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .transform(RoundedCorners(radiusInPx))
                .into(binding.pleerImageView)
        } else {
            Log.d(TAG, "Artwork URL is null or empty, setting placeholder")
            binding.pleerImageView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers called")

        lifecycleScope.launch {
            Log.d(TAG, "Starting collect for uiState")
            viewModel.uiState.collectLatest { state ->
                Log.d(TAG, "ViewModel uiState updated")
                // Проверяем, что _binding не null
                if (_binding != null) {
                    binding.otzetVremy.text = state.currentTime
                    binding.imageView.setImageResource(
                        if (state.isPlaying) R.drawable.buttonpase else R.drawable.button
                    )
                    binding.imageView3.setImageResource(
                        if (state.isLiked) R.drawable.button__4_ else R.drawable.button__3_
                    )
                    state.error?.let { error ->
                        Log.e(TAG, "ViewModel error: $error")
                        showError(error)
                    }
                } else {
                    Log.w(TAG, "uiState updated, but fragment view is destroyed, skipping UI update")
                }
            }
        }

        // Наблюдаем за результатом добавления в плейлист
        lifecycleScope.launch {
            Log.d(TAG, "Starting collect for addToPlaylistResult")
            viewModel.addToPlaylistResult.collectLatest { result ->
                result?.let {
                    Log.d(TAG, "ViewModel addToPlaylistResult received: $result")
                    // Проверяем, что _binding не null
                    if (_binding != null) {
                        when (it) {
                            // Используем напрямую AddToPlaylistResult
                            is AddToPlaylistResult.Success -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Добавлено в плейлист \"${it.playlistName}\"",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Закрываем BottomSheet только при успехе
                                bottomSheetDialog?.dismiss()
                                bottomSheetDialog = null
                            }
                            is AddToPlaylistResult.AlreadyExists -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Трек уже добавлен в плейлист \"${it.playlistName}\"", // Сообщение как в ТЗ
                                    Toast.LENGTH_SHORT
                                ).show()
                                // НЕ закрываем диалог, пользователь остается в BottomSheet
                                // Диалог остается открытым
                            }
                            is AddToPlaylistResult.Error -> {
                                Toast.makeText(
                                    requireContext(),
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Можно закрыть диалог при ошибке, или оставить для повторной попытки
                                // bottomSheetDialog?.dismiss()
                                // bottomSheetDialog = null
                            }
                        }
                        // Очищаем результат после отображения
                        viewModel.clearAddToPlaylistResult()
                    } else {
                        Log.w(TAG, "addToPlaylistResult received, but fragment view is destroyed, skipping UI update")
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")

        binding.imageView.setOnClickListener {
            Log.d(TAG, "Play/Pause button clicked")
            viewModel.togglePlayPause()
        }

        binding.imageView3.setOnClickListener {
            Log.d(TAG, "Like button clicked")
            viewModel.toggleLike()
        }

        binding.addToPlaylistButton.setOnClickListener {
            Log.d(TAG, "Add to playlist button clicked")
            showAddToPlaylistBottomSheet()
        }

        binding.backButton3.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            viewModel.releasePlayer()
            parentFragmentManager.popBackStack()
        }
    }

    private fun showAddToPlaylistBottomSheet() {
        Log.d(TAG, "showAddToPlaylistBottomSheet called")
        val currentTrack = viewModel.getCurrentTrack()

        if (currentTrack != null) {
            Log.d(TAG, "Current track found: ${currentTrack.trackName}")

            // Создаем и сохраняем ссылку на диалог, используя ваш стиль
            bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val binding = BottomSheetAddToPlaylistBinding.inflate(layoutInflater)
            bottomSheetDialog?.setContentView(binding.root)

            Log.d(TAG, "BottomSheetDialog created and content set")

            // Создаем адаптер для списка плейлистов
            val adapter = PlaylistBottomSheetAdapter(
                playlists = viewModel.playlistsState.value, // Получаем текущий список
                currentTrackId = currentTrack.trackId,
                onPlaylistClicked = { playlist ->
                    Log.d(TAG, "Playlist clicked in BottomSheet: ${playlist.name}")
                    // НЕ закрываем диалог здесь!
                    // bottomSheetDialog?.dismiss()
                    // Передаем только плейлист для обработки во ViewModel
                    viewModel.addTrackToPlaylist(playlist)
                    // Диалог будет закрыт в observers при успехе
                }
            )

            binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.playlistsRecyclerView.adapter = adapter

            Log.d(TAG, "Adapter set for playlistsRecyclerView")

            // Обновляем список плейлистов при изменении
            lifecycleScope.launch {
                Log.d(TAG, "Starting collect for playlistsState in BottomSheet")
                viewModel.playlistsState.collect { playlists ->
                    Log.d(TAG, "ViewModel playlistsState updated in BottomSheet: ${playlists.size} items")
                    // Проверяем, что _binding не null и диалог еще открыт
                    if (_binding != null && bottomSheetDialog?.isShowing == true) {
                        adapter.updatePlaylists(playlists)
                        Log.d(TAG, "Adapter playlists updated")
                    } else {
                        Log.w(TAG, "playlistsState updated, but fragment view is destroyed or dialog is not showing, skipping adapter update")
                    }
                }
            }

            // Загружаем плейлисты заново при открытии BottomSheet
            Log.d(TAG, "Refreshing playlists in ViewModel")
            viewModel.refreshPlaylists()

            binding.createNewPlaylistButton.setOnClickListener {
                Log.d(TAG, "Create new playlist button clicked in BottomSheet")
                // Закрываем диалог перед переходом
                bottomSheetDialog?.dismiss()
                bottomSheetDialog = null
                navigateToCreatePlaylist()
            }

            // Очищаем ссылку при закрытии диалога (например, свайпом вниз)
            bottomSheetDialog?.setOnDismissListener {
                Log.d(TAG, "BottomSheetDialog dismissed (via listener)")
                bottomSheetDialog = null
            }

            Log.d(TAG, "Showing BottomSheetDialog")
            bottomSheetDialog?.show()
        } else {
            Log.w(TAG, "Current track is null, cannot show BottomSheet")
            Toast.makeText(requireContext(), "Ошибка: трек не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCreatePlaylist() {
        Log.d(TAG, "navigateToCreatePlaylist called")
        try {
            // Используем глобальную навигацию или создаем action
            findNavController().navigate(R.id.action_global_createPlaylistFragment)
            Log.d(TAG, "Navigation to create playlist initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error", e)
            Toast.makeText(
                requireContext(),
                "Ошибка перехода: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, "showError called with message: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        if (viewModel.uiState.value.isPlaying) {
            Log.d(TAG, "Pausing playback")
            viewModel.togglePlayPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        // Закрываем диалог при уничтожении фрагмента, если он открыт
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        viewModel.releasePlayer()
        _binding = null
    }
}