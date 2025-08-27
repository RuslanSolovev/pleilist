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

import com.example.playlistmaker.presentation.viewmodel.PlaylistsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(), OnPlaylistLongClickListener {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var playlistsAdapter: PlaylistsAdapter

    private val TAG = "PlaylistsFragment_DEBUG"

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
        playlistsAdapter = PlaylistsAdapter(this)

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.layoutManager = layoutManager
        Log.d(TAG, "LayoutManager set: ${binding.playlistsRecyclerView.layoutManager}")

        binding.playlistsRecyclerView.adapter = playlistsAdapter
        Log.d(TAG, "Adapter set: ${binding.playlistsRecyclerView.adapter}")

        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_small)
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
            findNavController().navigate(R.id.action_global_createPlaylistFragment)
            Log.d(TAG, "Navigation initiated")
        } catch (e: Exception) {
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
    }

    // Реализация метода интерфейса OnPlaylistLongClickListener
    override fun onPlaylistLongClick(playlist: Playlist) {
        Log.d(TAG, "Long click on playlist: ${playlist.name} (ID: ${playlist.id})")
        showDeleteConfirmationDialog(playlist)
    }

    private fun showDeleteConfirmationDialog(playlist: Playlist) {
        Log.d(TAG, "Showing delete confirmation for playlist: ${playlist.name}")
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист?")
            .setMessage("Вы уверены, что хотите удалить плейлист \"${playlist.name}\"?")
            .setPositiveButton("Да") { _, _ ->
                Log.d(TAG, "User confirmed deletion of playlist: ${playlist.name} (ID: ${playlist.id})")
                viewModel.deletePlaylist(playlist.id)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
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