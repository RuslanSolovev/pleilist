package com.example.playlistmaker.presentation.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.presentation.adapter.OnPlaylistClickListener
import com.example.playlistmaker.presentation.adapter.OnPlaylistLongClickListener
import com.example.playlistmaker.presentation.adapter.PlaylistsAdapter
import com.example.playlistmaker.presentation.viewmodel.PlaylistsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(), OnPlaylistClickListener, OnPlaylistLongClickListener {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var playlistsAdapter: PlaylistsAdapter

    private val TAG = "PlaylistsFragment"

    // --- Callback для обработки клика (для взаимодействия с родительским фрагментом/активностью) ---
    /**
     * Интерфейс для обработки клика по элементу плейлиста.
     * Должен быть реализован родительским фрагментом (например, MediaLibraryFragment)
     * или активностью, если навигация управляется оттуда.
     */
    interface OnPlaylistItemClickListener {
        /**
         * Вызывается при клике на плейлист.
         * @param playlistId Идентификатор выбранного плейлиста.
         */
        fun onPlaylistItemClicked(playlistId: Long)
    }

    /**
     * Свойство для хранения ссылки на обработчик клика.
     * Может быть установлено родительским фрагментом или активностью.
     */
    var playlistItemClickListener: OnPlaylistItemClickListener? = null
    // ---------------------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        Log.d(TAG, "Binding inflated")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Toast.makeText(requireContext(), "PlaylistsFragment загружен", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView called")
        // Передаем оба листенера в адаптер (сам фрагмент реализует эти интерфейсы)
        playlistsAdapter = PlaylistsAdapter(this, this)

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.layoutManager = layoutManager
        Log.d(TAG, "LayoutManager set: ${binding.playlistsRecyclerView.layoutManager}")

        binding.playlistsRecyclerView.adapter = playlistsAdapter
        Log.d(TAG, "Adapter set: ${binding.playlistsRecyclerView.adapter}")

        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_small) // Убедитесь, что ресурс существует
        binding.playlistsRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(2, spacing, true)
        )
        Log.d(TAG, "ItemDecorator added")
        Log.d(TAG, "RecyclerView setup completed")
    }

    private fun setupListeners() {
        Log.d(TAG, "setupListeners called")
        binding.createPlaylistButton.setOnClickListener {
            Log.d(TAG, "Create playlist button clicked")
            navigateToCreatePlaylist()
        }
    }



    private fun navigateToCreatePlaylist() {
        try {
            Log.d(TAG, "Attempting navigation to create playlist")
            // Используем NavController напрямую для глобального действия
            // Убедитесь, что action_global_createPlaylistFragment определено в вашем nav_graph.xml
            findNavController().navigate(R.id.action_global_createPlaylistFragment)
            Log.d(TAG, "Navigation to create playlist initiated")
        } catch (e: Exception) {
            // Ловим любые исключения навигации
            Log.e(TAG, "Navigation error", e)
            Toast.makeText(
                requireContext(),
                "Ошибка перехода: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel called")

        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "Starting collect for playlists flow")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "repeatOnLifecycle STARTED for playlists")
                launch {
                    viewModel.playlists.collect { playlists ->
                        Log.d(TAG, "ViewModel playlists flow emitted: ${playlists.size} items")
                        if (_binding != null) {
                            Log.d(TAG, "Updating adapter with ${playlists.size} playlists")
                            playlistsAdapter.submitList(playlists)
                            Log.d(TAG, "Adapter updated, forcing UI state update")
                            // Принудительно обновляем UI состояние после получения списка
                            updateUIState(playlists.isEmpty())
                        } else {
                            Log.w(TAG, "Playlists updated, but fragment view is destroyed (_binding is null), skipping UI update")
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "Starting collect for isEmptyState flow")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "repeatOnLifecycle STARTED for isEmptyState")
                launch {
                    viewModel.isEmptyState.collect { isEmpty ->
                        Log.d(TAG, "ViewModel isEmptyState flow emitted: $isEmpty")
                        if (_binding != null) {
                            Log.d(TAG, "Updating UI state based on isEmptyState: $isEmpty")
                            updateUIState(isEmpty)
                        } else {
                            Log.w(TAG, "isEmptyState changed, but fragment view is destroyed (_binding is null), skipping UI update")
                        }
                    }
                }
            }
        }
    }

    // Централизованный метод для обновления состояния UI
    private fun updateUIState(isEmpty: Boolean) {
        Log.d(TAG, "updateUIState called with isEmpty=$isEmpty")
        if (_binding != null) {
            if (isEmpty) {
                showEmptyState()
            } else {
                showPlaylistsList()
            }
        } else {
            Log.w(TAG, "updateUIState: _binding is null, cannot update UI")
        }
    }

    private fun showEmptyState() {
        Log.d(TAG, "showEmptyState called")
        if (_binding != null) {
            try {
                Log.d(TAG, "Setting UI for EMPTY state")
                binding.placeholderImage.visibility = View.VISIBLE
                binding.placeholder.visibility = View.VISIBLE
                binding.createPlaylistButton.visibility = View.VISIBLE
                binding.playlistsRecyclerView.visibility = View.GONE
                Log.d(TAG, "Empty state UI set")
                logCurrentUIState("After showEmptyState")
            } catch (e: Exception) {
                Log.e(TAG, "Error in showEmptyState UI update", e)
            }
        } else {
            Log.w(TAG, "showEmptyState: _binding is null, cannot update UI")
        }
    }

    private fun showPlaylistsList() {
        Log.d(TAG, "showPlaylistsList called")
        if (_binding != null) {
            try {
                Log.d(TAG, "Setting UI for LIST state")
                binding.placeholderImage.visibility = View.GONE
                binding.placeholder.visibility = View.GONE
                // Кнопка создания остается видимой
                binding.createPlaylistButton.visibility = View.VISIBLE
                binding.playlistsRecyclerView.visibility = View.VISIBLE
                Log.d(TAG, "List state UI set")
                logCurrentUIState("After showPlaylistsList")
            } catch (e: Exception) {
                Log.e(TAG, "Error in showPlaylistsList UI update", e)
            }
        } else {
            Log.w(TAG, "showPlaylistsList: _binding is null, cannot update UI")
        }
    }

    private fun logCurrentUIState(tag: String) {
        if (_binding != null) {
            val rvVisibility = binding.playlistsRecyclerView.visibility
            val placeholderVisibility = binding.placeholder.visibility
            val imageVisibility = binding.placeholderImage.visibility
            val buttonVisibility = binding.createPlaylistButton.visibility
            val adapterItemCount = playlistsAdapter.itemCount
            Log.d(TAG, "$tag - UI state: RV: $rvVisibility, Placeholder: $placeholderVisibility, Image: $imageVisibility, Button: $buttonVisibility, Adapter items: $adapterItemCount")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called, refreshing playlists")
        logCurrentUIState("onResume start")
        viewModel.refreshPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
        // Очищаем ссылку на listener при уничтожении View
        playlistItemClickListener = null
    }

    // --- Реализация интерфейсов ---

    // Реализация метода интерфейса OnPlaylistClickListener
    override fun onPlaylistClick(playlist: Playlist) {
        Log.d(TAG, "Click on playlist: ${playlist.name} (ID: ${playlist.id})")

        // --- Используем callback для обработки клика ---
        // Это позволяет родительскому фрагменту (например, MediaLibraryFragment)
        // управлять навигацией, что решает проблему с NavController внутри ViewPager2
        playlistItemClickListener?.let { listener ->
            listener.onPlaylistItemClicked(playlist.id)
            Log.d(TAG, "Click handled via callback")
            return
        }

        // --- Альтернатива: Попытка вызова метода у родительского фрагмента напрямую ---
        // Это менее предпочтительный способ, но может работать в некоторых случаях
        try {
            val parent = parentFragment
            if (parent is OnPlaylistItemClickListener) {
                parent.onPlaylistItemClicked(playlist.id)
                Log.d(TAG, "Click handled via parent fragment direct call")
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not invoke parent method directly", e)
        }
        // ---------------------------------------------------------------

        // Если ни один из способов не сработал
        Log.w(TAG, "No listener found to handle playlist click. Navigation might fail.")
        Toast.makeText(requireContext(), "Навигация недоступна", Toast.LENGTH_SHORT).show()
        // Можно попытаться использовать findNavController(), но это может привести к ошибке,
        // как обсуждалось ранее.
        /*
        try {
            val bundle = Bundle().apply {
                putLong("playlistId", playlist.id)
            }
            findNavController().navigate(
                R.id.action_playlistsFragment_to_playlistDetailsFragment,
                bundle
            )
        } catch (e: Exception) {
            Log.e(TAG, "Direct navigation from PlaylistsFragment also failed", e)
            Toast.makeText(requireContext(), "Ошибка навигации", Toast.LENGTH_SHORT).show()
        }
        */
    }

    // Реализация метода интерфейса OnPlaylistLongClickListener
    override fun onPlaylistLongClick(playlist: Playlist) {
        Log.d(TAG, "Long click on playlist: ${playlist.name} (ID: ${playlist.id})")
        showDeleteConfirmationDialog(playlist)
    }

    private fun showDeleteConfirmationDialog(playlist: Playlist) {
        Log.d(TAG, "Showing delete confirmation for playlist: ${playlist.name}")
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист?") // Или используйте строковый ресурс
            .setMessage("Вы уверены, что хотите удалить плейлист \"${playlist.name}\"?") // Или используйте строковый ресурс
            .setPositiveButton("Да") { _, _ -> // Или используйте строковый ресурс
                Log.d(TAG, "User confirmed deletion of playlist: ${playlist.name} (ID: ${playlist.id})")
                viewModel.deletePlaylist(playlist.id)
            }
            .setNegativeButton("Отмена") { dialog, _ -> // Или используйте строковый ресурс
                Log.d(TAG, "User cancelled deletion of playlist: ${playlist.name}")
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }
}

// Класс для отступов между элементами GridLayout
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: androidx.recyclerview.widget.RecyclerView,
        state: androidx.recyclerview.widget.RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}