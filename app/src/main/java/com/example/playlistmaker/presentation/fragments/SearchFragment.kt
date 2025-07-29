package com.example.playlistmaker.presentation.fragments

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.ui.PlaceholderRenderer
import com.example.playlistmaker.presentation.viewmodel.SearchState
import com.example.playlistmaker.presentation.viewmodel.SearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var placeholderView: View
    private lateinit var retryButton: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyHeader: TextView
    private lateinit var clearHistoryButton: View
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var progressBar: ProgressBar

    private val navController by lazy {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
    }

    private lateinit var placeholderRenderer: PlaceholderRenderer

    private val viewModel: SearchViewModel by viewModel()

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerViews()
        setupListeners()

        placeholderRenderer = PlaceholderRenderer(
            requireContext(),
            placeholderView,
            view.findViewById(R.id.placeholder_image),
            view.findViewById(R.id.placeholder_text),
            retryButton
        )

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                render(state)
            }
        }

        savedInstanceState?.getString(SEARCH_QUERY_KEY)?.let { query ->
            searchEditText.setText(query)
            if (query.isNotEmpty()) {
                clearButton.visibility = View.VISIBLE
                viewModel.searchDebounced(query)
            } else {
                viewModel.restoreState()
            }
        } ?: run {
            viewModel.restoreState()
        }
    }

    private fun initViews(view: View) {
        searchEditText = view.findViewById(R.id.search_edit_text)
        clearButton = view.findViewById(R.id.clear_button)
        recyclerView = view.findViewById(R.id.recycler_view)
        placeholderView = view.findViewById(R.id.placeholder_view)
        retryButton = view.findViewById(R.id.retry_button)
        historyRecyclerView = view.findViewById(R.id.recycler_view_Istory)
        historyHeader = view.findViewById(R.id.Text_Istoriy)
        clearHistoryButton = view.findViewById(R.id.Ochistit_Istotiy)
        progressBar = view.findViewById(R.id.progressBar)

        view.findViewById<ImageButton>(R.id.back_button2).visibility = View.GONE
    }

    private fun setupRecyclerViews() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(requireContext()) { track -> handleTrackClick(track) }
        recyclerView.adapter = trackAdapter

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = TrackAdapter(requireContext()) { track -> handleTrackClick(track) }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isEmpty()) {
                    clearButton.visibility = View.GONE
                    viewModel.searchDebounced("")
                } else {
                    clearButton.visibility = View.VISIBLE
                    viewModel.searchDebounced(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard(searchEditText)
        }

        retryButton.setOnClickListener {
            viewModel.retrySearch()
        }

        view?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                activity?.currentFocus?.let { view ->
                    if (view is EditText) {
                        val rect = Rect()
                        view.getGlobalVisibleRect(rect)
                        if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                            hideKeyboard(view)
                            view.clearFocus()
                        }
                    }
                }
            }
            false
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchDebounced(query)
                    hideKeyboard(searchEditText)
                }
                true
            } else false
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                viewModel.restoreState()
            }
        }
    }

    private fun render(state: SearchState) {
        when (state) {
            SearchState.Loading -> {
                placeholderRenderer.showLoading()
                recyclerView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                hideHistory()
            }
            SearchState.NoResults -> {
                placeholderRenderer.showNoResults()
                recyclerView.visibility = View.GONE
                progressBar.visibility = View.GONE
                hideHistory()
            }
            SearchState.Error -> {
                placeholderRenderer.showError()
                recyclerView.visibility = View.GONE
                progressBar.visibility = View.GONE
                hideHistory()
            }
            is SearchState.Success -> {
                placeholderRenderer.hidePlaceholder()
                trackAdapter.updateTracks(state.tracks)
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                hideHistory()
            }
            is SearchState.ShowHistory -> {
                if (searchEditText.text.isEmpty()) {
                    placeholderRenderer.hidePlaceholder()
                    recyclerView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    showHistory(state.history)
                }
            }
            SearchState.Default -> {}
        }
    }

    private fun showHistory(history: List<Track>) {
        if (history.isNotEmpty()) {
            historyAdapter.updateTracks(history)
            historyRecyclerView.visibility = View.VISIBLE
            historyHeader.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
        } else {
            hideHistory()
        }
    }

    private fun hideHistory() {
        historyRecyclerView.visibility = View.GONE
        historyHeader.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun handleTrackClick(track: Track) {
        viewModel.handleTrackClick(track)

        val bundle = Bundle().apply {
            putInt("TRACK_ID", track.trackId)
            putString("TRACK_NAME", track.trackName)
            putString("ARTIST_NAME", track.artistName)
            putString("ARTWORK_URL", track.artworkUrl100)
            putString("COLLECTION_NAME", track.collectionName)
            putString("RELEASE_DATE", track.releaseDate)
            putString("PRIMARY_GENRE", track.primaryGenreName)
            putString("COUNTRY", track.country)
            track.trackTimeMillis?.let { putLong("TRACK_TIME_MILLIS", it) }
            putString("PREVIEW_URL", track.previewUrl)
        }

        try {
            navController.navigate(R.id.action_searchFragment_to_playerFragment, bundle)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchEditText.text.toString())
    }

    override fun onResume() {
        super.onResume()
        viewModel.restoreState()
    }
}