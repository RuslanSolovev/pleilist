package com.example.playlistmaker.presentation.activity

import android.content.*
import android.graphics.Rect
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.SearchState
import com.example.playlistmaker.presentation.SearchViewModel
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.ui.ClickDebounceHelper
import com.example.playlistmaker.presentation.ui.PlaceholderRenderer
import com.example.playlistmaker.presentation.ui.TrackNavigator

class SearchActivity : AppCompatActivity() {

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

    private lateinit var clickDebounce: ClickDebounceHelper
    private lateinit var trackNavigator: TrackNavigator
    private lateinit var placeholderRenderer: PlaceholderRenderer

    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val searchInteractor = Creator.provideSearchInteractor(applicationContext)
                    val historyInteractor = Creator.provideHistoryInteractor(applicationContext)
                    return SearchViewModel(searchInteractor, historyInteractor) as T
                }
            }
        )[SearchViewModel::class.java]
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupRecyclerViews()
        setupListeners()

        clickDebounce = ClickDebounceHelper(CLICK_DEBOUNCE_DELAY)
        trackNavigator = TrackNavigator(this)
        placeholderRenderer = PlaceholderRenderer(
            this,
            placeholderView,
            findViewById(R.id.placeholder_image),
            findViewById(R.id.placeholder_text),
            retryButton
        )

        viewModel.state.observe(this) { state ->
            render(state)
        }

        viewModel.history.observe(this) { history ->
            if (history.isNotEmpty()) {
                historyHeader.isVisible = true
                historyRecyclerView.isVisible = true
                recyclerView.isVisible = false
                clearHistoryButton.isVisible = true
                historyAdapter.updateTracks(history)
            } else {
                hideHistory()
            }
        }

        savedInstanceState?.getString(SEARCH_QUERY_KEY)?.let {
            searchEditText.setText(it)
            if (it.isNotEmpty()) {
                clearButton.visibility = View.VISIBLE
                viewModel.searchDebounced(it)
            } else {
                viewModel.loadHistory()
            }
        } ?: run {
            viewModel.loadHistory()
        }
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.clear_button)
        recyclerView = findViewById(R.id.recycler_view)
        placeholderView = findViewById(R.id.placeholder_view)
        retryButton = findViewById(R.id.retry_button)
        historyRecyclerView = findViewById(R.id.recycler_view_Istory)
        historyHeader = findViewById(R.id.Text_Istoriy)
        clearHistoryButton = findViewById(R.id.Ochistit_Istotiy)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.back_button2).setOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(this, emptyList()) { handleTrackClick(it) }
        recyclerView.adapter = trackAdapter

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(this, emptyList()) { handleTrackClick(it) }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isEmpty()) {
                    clearButton.isVisible = false
                    // Удаляем ручное управление UI, пусть ViewModel решает что показывать
                    viewModel.searchDebounced("") // Пустая строка переведет в состояние ShowHistory
                } else {
                    clearButton.isVisible = true
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
            viewModel.retry()
        }

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                currentFocus?.let { view ->
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
            if (hasFocus && searchEditText.text.isEmpty()) viewModel.loadHistory()
        }
    }
    private fun showHistory() {
        val history = viewModel.history.value ?: emptyList()
        if (history.isNotEmpty()) {
            historyAdapter.updateTracks(history)
            historyRecyclerView.isVisible = true
            historyHeader.isVisible = true
            clearHistoryButton.isVisible = true
        } else {
            hideHistory()
        }
    }




    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Loading -> {
                placeholderRenderer.showLoading()
                recyclerView.isVisible = false
                hideHistory()
                progressBar.isVisible = true
            }

            is SearchState.NoResults -> {
                placeholderRenderer.showNoResults()
                recyclerView.isVisible = false
                hideHistory()
                progressBar.isVisible = false
            }

            is SearchState.Error -> {
                placeholderRenderer.showError()
                recyclerView.isVisible = false
                hideHistory()
                progressBar.isVisible = false
            }

            is SearchState.Success -> {
                placeholderRenderer.hidePlaceholder()
                trackAdapter.updateTracks(state.tracks)
                progressBar.isVisible = false
                recyclerView.isVisible = true
                hideHistory()
            }

            is SearchState.Empty -> {
                placeholderRenderer.hidePlaceholder()
                trackAdapter.updateTracks(emptyList())
                progressBar.isVisible = false
                recyclerView.isVisible = false
                hideHistory()
            }

            is SearchState.ShowHistory -> {
                placeholderRenderer.hidePlaceholder()
                recyclerView.isVisible = false
                progressBar.isVisible = false
                showHistory()
            }


    else -> {}
        }
    }

    private fun hideHistory() {
        historyHeader.isVisible = false
        historyRecyclerView.isVisible = false
        clearHistoryButton.isVisible = false
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun handleTrackClick(track: Track) {
        if (!clickDebounce.canClick()) return
        viewModel.saveTrackToHistory(track)
        trackNavigator.openTrack(track)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchEditText.text.toString())
    }

    override fun onResume() {
        super.onResume()
        if (searchEditText.text.isEmpty()) {
            viewModel.loadHistory()

        }
        viewModel.restoreSearch()
    }
}
