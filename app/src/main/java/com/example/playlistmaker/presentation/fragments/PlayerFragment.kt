package com.example.playlistmaker.presentation.fragments

import PlaylistBottomSheetAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
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

    private val viewModel: MediaViewModel by viewModel()

    private lateinit var currentTimeTextView: TextView
    private lateinit var playPauseButton: ImageView
    private lateinit var likeButton: ImageView
    private lateinit var addToPlaylistButton: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var albumTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var genreTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var artworkImageView: ImageView

    // Переменная для хранения ссылки на BottomSheetDialog
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupObservers()
        setupClickListeners(view)
        loadTrackData()
        // Загружаем плейлисты при открытии фрагмента
        viewModel.refreshPlaylists()
    }

    private fun initViews(view: View) {
        currentTimeTextView = view.findViewById(R.id.otzet_vremy)
        playPauseButton = view.findViewById(R.id.imageView)
        likeButton = view.findViewById(R.id.imageView3)
        addToPlaylistButton = view.findViewById(R.id.add_to_playlist_button)
        trackNameTextView = view.findViewById(R.id.pesny_nazvanie)
        artistNameTextView = view.findViewById(R.id.nazvanie_gruppa)
        albumTextView = view.findViewById(R.id.albom2)
        yearTextView = view.findViewById(R.id.god2)
        genreTextView = view.findViewById(R.id.janr2)
        countryTextView = view.findViewById(R.id.strana2)
        durationTextView = view.findViewById(R.id.dlitelnost2)
        artworkImageView = view.findViewById(R.id.pleer_image_view)
    }

    private fun loadTrackData() {
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

        previewUrl?.let { viewModel.preparePlayer(it) }

        trackNameTextView.text = trackName ?: getString(R.string.unknown_track)
        artistNameTextView.text = artistName ?: getString(R.string.unknown_artist)
        albumTextView.text = collectionName ?: getString(R.string.unknown_album)
        yearTextView.text = releaseDate?.take(4) ?: getString(R.string.unknown_year)
        genreTextView.text = primaryGenre ?: getString(R.string.unknown_genre)
        countryTextView.text = country ?: getString(R.string.unknown_country)
        durationTextView.text = TimeFormatter.formatTrackTime(trackTimeMillis ?: 0L)

        if (!artworkUrl.isNullOrEmpty()) {
            val radiusInPx = resources.getDimensionPixelSize(R.dimen.corner_radius_big)
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .transform(RoundedCorners(radiusInPx))
                .into(artworkImageView)
        } else {
            artworkImageView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                currentTimeTextView.text = state.currentTime
                playPauseButton.setImageResource(
                    if (state.isPlaying) R.drawable.buttonpase else R.drawable.button
                )
                likeButton.setImageResource(
                    if (state.isLiked) R.drawable.button__4_ else R.drawable.button__3_
                )
                state.error?.let { error ->
                    showError(error)
                }
            }
        }

        // Наблюдаем за результатом добавления в плейлист
        lifecycleScope.launch {
            viewModel.addToPlaylistResult.collectLatest { result ->
                result?.let {
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
                }
            }
        }

        // Наблюдаем за состоянием плейлистов (для BottomSheet)
        // В вашем случае адаптер будет обновляться отдельно
    }

    private fun setupClickListeners(view: View) {
        playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        likeButton.setOnClickListener {
            viewModel.toggleLike()
        }

        addToPlaylistButton.setOnClickListener {
            showAddToPlaylistBottomSheet()
        }

        view.findViewById<ImageButton>(R.id.back_button3).setOnClickListener {
            viewModel.releasePlayer()
            parentFragmentManager.popBackStack()
        }
    }

    private fun showAddToPlaylistBottomSheet() {
        // Создаем и сохраняем ссылку на диалог
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetAddToPlaylistBinding.inflate(layoutInflater)
        bottomSheetDialog?.setContentView(binding.root)

        val currentTrack = viewModel.getCurrentTrack()

        if (currentTrack != null) {
            // Создаем адаптер для списка плейлистов
            val adapter = PlaylistBottomSheetAdapter(
                playlists = viewModel.playlistsState.value, // Получаем текущий список
                currentTrackId = currentTrack.trackId,
                onPlaylistClicked = { playlist ->
                    // НЕ закрываем диалог здесь!
                    // bottomSheetDialog?.dismiss()
                    // Передаем только плейлист для обработки во ViewModel
                    viewModel.addTrackToPlaylist(playlist)
                    // Диалог будет закрыт в observers при успехе
                }
            )

            binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.playlistsRecyclerView.adapter = adapter

            // Обновляем список плейлистов при изменении
            lifecycleScope.launch {
                viewModel.playlistsState.collect { playlists ->
                    adapter.updatePlaylists(playlists)
                }
            }

            // Загружаем плейлисты заново при открытии BottomSheet
            viewModel.refreshPlaylists()
        }

        binding.createNewPlaylistButton.setOnClickListener {
            // Закрываем диалог перед переходом
            bottomSheetDialog?.dismiss()
            bottomSheetDialog = null
            navigateToCreatePlaylist()
        }

        // Очищаем ссылку при закрытии диалога (например, свайпом вниз)
        bottomSheetDialog?.setOnDismissListener {
            bottomSheetDialog = null
        }

        bottomSheetDialog?.show()
    }

    private fun navigateToCreatePlaylist() {
        try {
            // Используем глобальную навигацию
            findNavController().navigate(R.id.action_global_createPlaylistFragment)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Ошибка перехода: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.uiState.value.isPlaying) {
            viewModel.togglePlayPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Закрываем диалог при уничтожении фрагмента, если он открыт
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        viewModel.releasePlayer()
    }
}