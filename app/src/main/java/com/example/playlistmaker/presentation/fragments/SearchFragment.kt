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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.SearchState
import com.example.playlistmaker.presentation.SearchViewModel
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.ui.ClickDebounceHelper
import com.example.playlistmaker.presentation.ui.PlaceholderRenderer
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    // UI элементы
    private var searchEditText: EditText? = null
    private var clearButton: ImageButton? = null
    private var recyclerView: RecyclerView? = null
    private var placeholderView: View? = null
    private var retryButton: View? = null
    private var historyRecyclerView: RecyclerView? = null
    private var historyHeader: TextView? = null
    private var clearHistoryButton: View? = null
    private var progressBar: ProgressBar? = null

    // Адаптеры
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    // Вспомогательные классы
    private lateinit var clickDebounce: ClickDebounceHelper
    private lateinit var placeholderRenderer: PlaceholderRenderer
    private lateinit var navController: NavController

    private val viewModel: SearchViewModel by viewModel()

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_search, container, false)
        initViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        clickDebounce = ClickDebounceHelper(CLICK_DEBOUNCE_DELAY)

        placeholderRenderer = PlaceholderRenderer(
            requireContext(),
            placeholderView ?: return,
            view.findViewById(R.id.placeholder_image),
            view.findViewById(R.id.placeholder_text),
            retryButton ?: return
        )

        setupRecyclerViews()
        setupListeners()

        // Наблюдаем за состоянием ViewModel
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        // Восстанавливаем состояние после поворота экрана
        savedInstanceState?.getString(SEARCH_QUERY_KEY)?.let { query ->
            searchEditText?.setText(query)
            if (query.isNotEmpty()) {
                clearButton?.visibility = View.VISIBLE
                viewModel.searchDebounced(query)
            } else {
                viewModel.restoreState()
            }
        } ?: viewModel.restoreState()
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

        // Скрываем кнопку назад, если она не нужна
        view.findViewById<ImageButton>(R.id.back_button2)?.visibility = View.GONE
    }

    private fun setupRecyclerViews() {
        // Настройка RecyclerView для результатов поиска
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(requireContext(), emptyList()) { track ->
            if (clickDebounce.canClick()) {
                handleTrackClick(track)
            }
        }
        recyclerView?.adapter = trackAdapter

        // Настройка RecyclerView для истории поиска
        historyRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = TrackAdapter(requireContext(), emptyList()) { track ->
            if (clickDebounce.canClick()) {
                handleTrackClick(track)
            }
        }
        historyRecyclerView?.adapter = historyAdapter
    }

    private fun setupListeners() {
        // Слушатель изменений текста в поле поиска
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.isEmpty()) {
                    clearButton?.visibility = View.GONE
                    viewModel.searchDebounced("")
                } else {
                    clearButton?.visibility = View.VISIBLE
                    viewModel.searchDebounced(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // Кнопка очистки поиска
        clearButton?.setOnClickListener {
            searchEditText?.setText("")
            searchEditText?.let { hideKeyboard(it) }
        }

        // Кнопка повтора при ошибке
        retryButton?.setOnClickListener {
            viewModel.retry()
        }

        // Скрытие клавиатуры при касании вне поля ввода
        view?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                activity?.currentFocus?.let { focusedView ->
                    if (focusedView is EditText) {
                        val rect = Rect()
                        focusedView.getGlobalVisibleRect(rect)
                        if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                            hideKeyboard(focusedView)
                            focusedView.clearFocus()
                        }
                    }
                }
            }
            false
        }

        // Обработка нажатия "Поиск" на клавиатуре
        searchEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText?.text?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    viewModel.searchDebounced(query)
                    searchEditText?.let { hideKeyboard(it) }
                }
                true
            } else false
        }

        // Кнопка очистки истории
        clearHistoryButton?.setOnClickListener {
            viewModel.clearHistory()
        }

        // Обработка фокуса на поле поиска
        searchEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText?.text?.isEmpty() == true) {
                viewModel.restoreState()
            }
        }
    }

    private fun render(state: SearchState) {
        when (state) {
            SearchState.Loading -> {
                placeholderRenderer.showLoading()
                recyclerView?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
                hideHistory()
            }
            SearchState.NoResults -> {
                placeholderRenderer.showNoResults()
                recyclerView?.visibility = View.GONE
                progressBar?.visibility = View.GONE
                hideHistory()
            }
            SearchState.Error -> {
                placeholderRenderer.showError()
                recyclerView?.visibility = View.GONE
                progressBar?.visibility = View.GONE
                hideHistory()
            }
            is SearchState.Success -> {
                placeholderRenderer.hidePlaceholder()
                trackAdapter.updateTracks(state.tracks)
                progressBar?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                hideHistory()
            }
            is SearchState.ShowHistory -> {
                if (searchEditText?.text?.isEmpty() == true) {
                    placeholderRenderer.hidePlaceholder()
                    recyclerView?.visibility = View.GONE
                    progressBar?.visibility = View.GONE
                    showHistory(state.history)
                }
            }
            SearchState.Default -> {}
            else -> {}
        }
    }

    private fun showHistory(history: List<Track>) {
        if (history.isNotEmpty()) {
            historyAdapter.updateTracks(history)
            historyRecyclerView?.visibility = View.VISIBLE
            historyHeader?.visibility = View.VISIBLE
            clearHistoryButton?.visibility = View.VISIBLE
        } else {
            hideHistory()
        }
    }

    private fun hideHistory() {
        historyRecyclerView?.visibility = View.GONE
        historyHeader?.visibility = View.GONE
        clearHistoryButton?.visibility = View.GONE
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun handleTrackClick(track: Track) {
        // Проверяем, доступен ли трек для проигрывания
        if (track.previewUrl.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.preview_not_available),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Сохраняем трек в историю
        viewModel.saveTrackToHistory(track)

        // Создаем Bundle с данными трека
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

        // Переходим к фрагменту плеера
        try {
            navController.navigate(R.id.action_searchFragment_to_playerFragment, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                getString(R.string.error_opening_player),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        searchEditText?.text?.toString()?.let {
            outState.putString(SEARCH_QUERY_KEY, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылки на view для избежания утечек памяти
        searchEditText = null
        clearButton = null
        recyclerView = null
        placeholderView = null
        retryButton = null
        historyRecyclerView = null
        historyHeader = null
        clearHistoryButton = null
        progressBar = null
    }
}