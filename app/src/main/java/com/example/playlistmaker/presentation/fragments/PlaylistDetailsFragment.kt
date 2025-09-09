package com.example.playlistmaker.presentation.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistDetailsBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapter.OnTrackClickListener
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.viewmodel.PlaylistDetailsViewModel
import com.example.playlistmaker.presentation.viewmodel.PlaylistDetailsState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailsFragment : Fragment(), OnTrackClickListener {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailsViewModel by viewModel()
    private lateinit var tracksAdapter: TrackAdapter
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<androidx.constraintlayout.widget.ConstraintLayout>

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
        private const val TAG = "PlaylistDetailsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        Log.d(TAG, "Binding inflated")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: run {
            Log.e(TAG, "Playlist ID not found in arguments")
            Toast.makeText(context, "Ошибка: ID плейлиста не найден", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }
        Log.d(TAG, "Received playlistId: $playlistId")

        setupBottomSheet()
        setupTracksRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadPlaylist(playlistId)
        Log.d(TAG, "loadPlaylist called with ID: $playlistId")
    }

    private fun setupBottomSheet() {
        Log.d(TAG, "setupBottomSheet called")
        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet)
        Log.d(TAG, "BottomSheetBehavior obtained")

        tracksBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d(TAG, "BottomSheet state changed: $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Log.d(TAG, "BottomSheet slide offset: $slideOffset")
            }
        })

        tracksBottomSheetBehavior.isHideable = false
        tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        Log.d(TAG, "BottomSheetBehavior configured (isHideable=false, state=COLLAPSED)")
    }

    private fun setupTracksRecyclerView() {
        Log.d(TAG, "setupTracksRecyclerView called")
        tracksAdapter = TrackAdapter(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            onTrackClickListener = this
        )

        val layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.layoutManager = layoutManager
        binding.tracksRecyclerView.adapter = tracksAdapter
        // Изначально скрываем RecyclerView и показываем сообщение
        binding.tracksRecyclerView.visibility = View.GONE
        binding.emptyTracksMessageTextView.visibility = View.VISIBLE
        Log.d(TAG, "Tracks RecyclerView inside BottomSheet setup completed")
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")

        binding.backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            findNavController().popBackStack()
        }

        binding.shareButton.setOnClickListener {
            Log.d(TAG, "Share button clicked")
            handleShareButtonClick()
        }

        binding.menuButton.setOnClickListener {
            Log.d(TAG, "Menu button clicked")
            showPlaylistMenu()
        }

        binding.sheetHandle.setOnClickListener {
            Log.d(TAG, "Bottom sheet handle clicked")
            if (tracksBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel called")

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                Log.d(TAG, "repeatOnLifecycle STARTED for state flow")
                viewModel.state.collect { state ->
                    Log.d(TAG, "ViewModel state emitted: ${state.javaClass.simpleName}")
                    when (state) {
                        is PlaylistDetailsState.Loading -> {
                            Log.d(TAG, "State: Loading")
                            showLoadingState()
                        }
                        is PlaylistDetailsState.Content -> {
                            Log.d(TAG, "State: Content with ${state.tracks.size} tracks")
                            bindContent(state)
                            showContentState()
                            if (state.tracks.isNotEmpty()) {
                                tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                Log.d(TAG, "BottomSheet made visible/collapsed due to content")
                            } else {
                                tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                Log.d(TAG, "BottomSheet collapsed due to no content")
                            }
                        }
                        is PlaylistDetailsState.Error -> {
                            Log.e(TAG, "State: Error - ${state.message}")
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            showErrorState(state.message)
                            tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                }
            }
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
        binding.tracksBottomSheet.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE
    }

    private fun showContentState() {
        binding.progressBar.visibility = View.GONE
        binding.contentContainer.visibility = View.VISIBLE
        binding.tracksBottomSheet.visibility = View.VISIBLE
        binding.errorContainer.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentContainer.visibility = View.GONE
        binding.tracksBottomSheet.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE
        binding.errorTextView.text = message
    }

    private fun bindContent(content: PlaylistDetailsState.Content) {
        val playlist = content.playlist
        Log.d(TAG, "Binding content for playlist: ${playlist.name}")

        if (!playlist.coverImagePath.isNullOrBlank()) {
            Log.d(TAG, "Loading cover image from path: ${playlist.coverImagePath}")
            Glide.with(binding.playlistCoverImageView)
                .load(playlist.coverImagePath)
                .apply(RequestOptions().transform(RoundedCorners(16)))
                .placeholder(R.drawable.placeholder_vector)
                .error(R.drawable.placeholder_vector)
                .into(binding.playlistCoverImageView)
        } else {
            Log.d(TAG, "No cover image, setting placeholder")
            binding.playlistCoverImageView.setImageResource(R.drawable.placeholder_vector)
        }

        binding.playlistNameTextView.text = playlist.name

        if (!playlist.description.isNullOrBlank()) {
            binding.playlistDescriptionTextView.text = playlist.description
            binding.playlistDescriptionTextView.visibility = View.VISIBLE
        } else {
            binding.playlistDescriptionTextView.visibility = View.GONE
        }

        val tracksCountText = when (playlist.tracksCount) {
            0 -> getString(R.string.tracks_count_zero)
            1 -> getString(R.string.tracks_count_one, playlist.tracksCount)
            2, 3, 4 -> getString(R.string.tracks_count_few, playlist.tracksCount)
            else -> {
                val lastDigit = playlist.tracksCount % 10
                val lastTwoDigits = playlist.tracksCount % 100
                when {
                    lastTwoDigits in 11..14 -> getString(R.string.tracks_count_many, playlist.tracksCount)
                    lastDigit == 1 -> getString(R.string.tracks_count_one, playlist.tracksCount)
                    lastDigit in 2..4 -> getString(R.string.tracks_count_few, playlist.tracksCount)
                    else -> getString(R.string.tracks_count_many, playlist.tracksCount)
                }
            }
        }
        Log.d(TAG, "Formatted tracks count text: $tracksCountText")

        val totalDurationFormatted = formatDurationFromMillis(content.totalDurationMillis)
        Log.d(TAG, "Formatted total duration text: $totalDurationFormatted")

        val durationTextView = binding.tracksInfoContainer.getChildAt(0) as? TextView
        val tracksCountTextView = binding.tracksInfoContainer.getChildAt(2) as? TextView

        durationTextView?.text = totalDurationFormatted
        tracksCountTextView?.text = tracksCountText

        // --- Обновление списка треков и сообщения о пустом списке ---
        if (content.tracks.isNotEmpty()) {
            binding.tracksRecyclerView.visibility = View.VISIBLE
            binding.emptyTracksMessageTextView.visibility = View.GONE
            tracksAdapter.updateTracks(content.tracks)
            Log.d(TAG, "Tracks adapter in BottomSheet updated with ${content.tracks.size} items")
        } else {
            binding.tracksRecyclerView.visibility = View.GONE
            binding.emptyTracksMessageTextView.visibility = View.VISIBLE
            tracksAdapter.updateTracks(emptyList())
            Log.d(TAG, "Tracks list is empty, showing empty message")
        }
        // -------------------------------------------------------------

        // --- Обновляем информацию в меню ---
        updateMenuInfo(playlist)
        // ----------------------------------
    }

    private fun formatDurationFromMillis(millis: Long): String {
        if (millis <= 0) {
            return getString(R.string.total_duration_minutes_zero)
        }

        val totalSeconds = millis / 1000
        val totalMinutes = totalSeconds / 60

        return when (totalMinutes) {
            0L -> getString(R.string.total_duration_minutes_zero)
            1L -> getString(R.string.total_duration_minutes_one, totalMinutes)
            2L, 3L, 4L -> getString(R.string.total_duration_minutes_few, totalMinutes)
            else -> {
                val lastDigit = (totalMinutes % 10).toInt()
                val lastTwoDigits = (totalMinutes % 100).toInt()
                when {
                    lastTwoDigits in 11..14 || lastDigit == 0 || lastDigit in 5..9 -> getString(R.string.total_duration_minutes_many, totalMinutes)
                    lastDigit == 1 -> getString(R.string.total_duration_minutes_one, totalMinutes)
                    lastDigit in 2..4 -> getString(R.string.total_duration_minutes_few, totalMinutes)
                    else -> getString(R.string.total_duration_minutes_many, totalMinutes)
                }
            }
        }
    }

    // --- НОВЫЕ МЕТОДЫ ---

    private fun updateMenuInfo(playlist: com.example.playlistmaker.domain.model.Playlist) {
        Log.d(TAG, "updateMenuInfo called for playlist: ${playlist.name}")
        // Информация обновляется при открытии меню в showPlaylistMenu()
        // Здесь можно сохранить playlist в поле класса, если нужно
        // currentPlaylist = playlist
        Log.d(TAG, "Menu info will be updated when menu is shown")
    }

    private fun showPlaylistMenu() {
        Log.d(TAG, "showPlaylistMenu called")

        val currentState = viewModel.state.value
        if (currentState !is PlaylistDetailsState.Content) {
            Log.w(TAG, "Cannot show menu: state is not Content")
            Toast.makeText(context, "Ошибка: данные плейлиста недоступны", Toast.LENGTH_SHORT).show()
            return
        }

        val playlist = currentState.playlist

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.RoundedBottomSheetDialog)
        val bottomSheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_playlist_menu, null)

        // --- Обновляем информацию в меню ---
        val menuPlaylistCoverImageView: ImageView = bottomSheetView.findViewById(R.id.menuPlaylistCoverImageView)
        val menuPlaylistNameTextView: TextView = bottomSheetView.findViewById(R.id.menuPlaylistNameTextView)
        val menuTracksCountTextView: TextView = bottomSheetView.findViewById(R.id.menuTracksCountTextView)

        if (!playlist.coverImagePath.isNullOrBlank()) {
            Glide.with(menuPlaylistCoverImageView.context)
                .load(playlist.coverImagePath)
                .apply(RequestOptions().centerCrop().transform(RoundedCorners(8)))
                .placeholder(R.drawable.placeholder_vector)
                .error(R.drawable.placeholder_vector)
                .into(menuPlaylistCoverImageView)
        } else {
            menuPlaylistCoverImageView.setImageResource(R.drawable.placeholder_vector)
        }

        menuPlaylistNameTextView.text = playlist.name

        val menuTracksCountText = when (playlist.tracksCount) {
            0 -> getString(R.string.tracks_count_zero)
            1 -> getString(R.string.tracks_count_one, playlist.tracksCount)
            2, 3, 4 -> getString(R.string.tracks_count_few, playlist.tracksCount)
            else -> {
                val lastDigit = playlist.tracksCount % 10
                val lastTwoDigits = playlist.tracksCount % 100
                when {
                    lastTwoDigits in 11..14 -> getString(R.string.tracks_count_many, playlist.tracksCount)
                    lastDigit == 1 -> getString(R.string.tracks_count_one, playlist.tracksCount)
                    lastDigit in 2..4 -> getString(R.string.tracks_count_few, playlist.tracksCount)
                    else -> getString(R.string.tracks_count_many, playlist.tracksCount)
                }
            }
        }
        menuTracksCountTextView.text = menuTracksCountText
        // ----------------------------------

        // --- Обработчики кликов в меню ---
        val menuShareButton: TextView = bottomSheetView.findViewById(R.id.menuShareButton)
        val menuEditInfoButton: TextView = bottomSheetView.findViewById(R.id.menuEditInfoButton)
        val menuDeletePlaylistButton: TextView = bottomSheetView.findViewById(R.id.menuDeletePlaylistButton)

        menuShareButton.setOnClickListener {
            Log.d(TAG, "Menu Share button clicked")
            bottomSheetDialog.dismiss()
            handleShareButtonClick() // Переиспользуем логику
        }

        menuEditInfoButton.setOnClickListener {
            Log.d(TAG, "Menu Edit Info button clicked")
            bottomSheetDialog.dismiss()

            // Получаем ID текущего плейлиста
            val currentState = viewModel.state.value
            if (currentState is PlaylistDetailsState.Content) {
                val playlistId = currentState.playlist.id
                // Создаём действие навигации с аргументом
                val bundle = Bundle().apply {
                    putLong("playlistId", playlistId)
                }
                // Выполняем переход, используя ID фрагмента назначения и Bundle
                try {
                    findNavController().navigate(R.id.editPlaylistFragment, bundle)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to EditPlaylistFragment", e)
                    Toast.makeText(context, "Ошибка перехода к редактированию", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Ошибка: данные плейлиста недоступны", Toast.LENGTH_SHORT).show()
            }
        }

        menuDeletePlaylistButton.setOnClickListener {
            Log.d(TAG, "Menu Delete Playlist button clicked")
            bottomSheetDialog.dismiss()
            showDeletePlaylistConfirmationDialog(playlist)
        }
        // ----------------------------------

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
        Log.d(TAG, "Playlist menu BottomSheetDialog shown")
    }

    private fun handleShareButtonClick() {
        Log.d(TAG, "handleShareButtonClick called")

        val currentState = viewModel.state.value
        if (currentState is PlaylistDetailsState.Content) {
            val playlist = currentState.playlist
            val tracks = currentState.tracks

            if (tracks.isEmpty()) {
                Log.d(TAG, "Share button clicked, but playlist is empty")
                Toast.makeText(requireContext(), "В этом плейлисте нет списка треков, которым можно поделиться", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Preparing share text for playlist: ${playlist.name} with ${tracks.size} tracks")
            val shareText = buildShareText(playlist, tracks)
            Log.d(TAG, "Share text prepared: $shareText")

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            try {
                startActivity(shareIntent)
                Log.d(TAG, "Share intent started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting share intent", e)
                Toast.makeText(requireContext(), "Не удалось открыть меню шаринга", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Share button clicked, but state is not Content: ${currentState.javaClass.simpleName}")
            Toast.makeText(requireContext(), "Невозможно поделиться: данные плейлиста не загружены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildShareText(playlist: com.example.playlistmaker.domain.model.Playlist, tracks: List<Track>): String {
        Log.d(TAG, "buildShareText called for playlist: ${playlist.name} with ${tracks.size} tracks")

        val sb = StringBuilder()

        // Название плейлиста
        sb.appendLine(playlist.name)

        // Описание (если есть)
        if (!playlist.description.isNullOrBlank()) {
            sb.appendLine(playlist.description)
        }

        // Количество треков
        val tracksCountText = when (playlist.tracksCount) {
            0 -> getString(R.string.tracks_count_zero)
            1 -> getString(R.string.tracks_count_one, playlist.tracksCount)
            2, 3, 4 -> getString(R.string.tracks_count_few, playlist.tracksCount)
            else -> {
                val lastDigit = playlist.tracksCount % 10
                val lastTwoDigits = playlist.tracksCount % 100
                when {
                    lastTwoDigits in 11..14 -> getString(R.string.tracks_count_many, playlist.tracksCount)
                    lastDigit == 1 -> getString(R.string.tracks_count_one, playlist.tracksCount)
                    lastDigit in 2..4 -> getString(R.string.tracks_count_few, playlist.tracksCount)
                    else -> getString(R.string.tracks_count_many, playlist.tracksCount)
                }
            }
        }
        sb.appendLine(tracksCountText)

        // Пронумерованный список треков
        tracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val artistName = track.artistName ?: getString(R.string.unknown_artist)
            val trackName = track.trackName ?: getString(R.string.unknown_track)
            val trackTime = track.trackTime ?: "--:--"

            sb.appendLine("$trackNumber. $artistName - $trackName ($trackTime)")
        }

        Log.d(TAG, "Share text built with length: ${sb.length}")
        return sb.toString().trimEnd() // Убираем последний \n
    }

    private fun showDeletePlaylistConfirmationDialog(playlist: com.example.playlistmaker.domain.model.Playlist) {
        Log.d(TAG, "showDeletePlaylistConfirmationDialog called for playlist: ${playlist.name}")

        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист?") // Или используйте строковый ресурс
            .setMessage("Вы уверены, что хотите удалить плейлист \"${playlist.name}\"?") // Или используйте строковый ресурс
            .setPositiveButton("ДА") { _, _ -> // Или используйте строковый ресурс
                Log.d(TAG, "User confirmed deletion of playlist: ${playlist.name} (ID: ${playlist.id})")
                viewModel.deletePlaylist(playlist.id)
                findNavController().popBackStack() // Возвращаемся на экран "Медиатека"
            }
            .setNegativeButton("НЕТ") { dialog, _ -> // Или используйте строковый ресурс
                Log.d(TAG, "User cancelled deletion of playlist: ${playlist.name}")
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    // --- Реализация методов интерфейса OnTrackClickListener ---
    override fun onItemClick(track: Track) {
        Log.d(TAG, "Track clicked: ${track.trackName} - ${track.artistName}")
        // Toast.makeText(requireContext(), "Track: ${track.trackName}", Toast.LENGTH_SHORT).show()

        // Создаем Bundle с аргументами
        val bundle = Bundle().apply {
            // Убедитесь, что trackId типа Int. Если Long, используйте putLong
            putInt("TRACK_ID", track.trackId)
        }

        try {
            // Переходим к PlayerFragment, передавая аргументы в Bundle
            // Убедитесь, что R.id.playerFragment - это правильный ID из вашего nav_graph.xml
            findNavController().navigate(R.id.playerFragment, bundle)
            Log.d(TAG, "Navigating to PlayerFragment with trackId: ${track.trackId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to PlayerFragment", e)
            Toast.makeText(requireContext(), "Ошибка перехода к плееру", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemLongClick(track: Track) {
        Log.d(TAG, "Track long clicked: ${track.trackName} - ${track.artistName}")
        showDeleteTrackConfirmationDialog(track)
    }
    // -------------------------------------------------------------

    private fun showDeleteTrackConfirmationDialog(track: Track) {
        Log.d(TAG, "Showing delete confirmation dialog for track: ${track.trackName}")
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить трек?") // Или используйте строковый ресурс
            .setMessage("Вы уверены, что хотите удалить трек \"${track.trackName}\"?") // Или используйте строковый ресурс
            .setPositiveButton("ДА") { _, _ -> // Или используйте строковый ресурс
                Log.d(TAG, "User confirmed deletion of track: ${track.trackName} (ID: ${track.trackId})")
                val playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: run {
                    Log.e(TAG, "Cannot delete track: Playlist ID not available")
                    Toast.makeText(context, "Ошибка: ID плейлиста недоступен для удаления", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.removeTrackFromPlaylist(track.trackId, playlistId)
            }
            .setNegativeButton("НЕТ") { dialog, _ -> // Или используйте строковый ресурс
                Log.d(TAG, "User cancelled deletion of track: ${track.trackName}")
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
}