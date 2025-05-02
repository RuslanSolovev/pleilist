package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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
    private lateinit var searchHistory: SearchHistory
    private var searchQuery: String? = null
    private val itunesApiService by lazy { createItunesApiService() }
    private  lateinit var progressBar: ProgressBar
    private var isClickAllowed = true
    private val clickHandler = Handler(Looper.getMainLooper())

    // Handler и Runnable для реализации debounce
    private val searchHandler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        searchQuery?.let { query ->
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }
    }



    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация UI элементов
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.clear_button)
        recyclerView = findViewById(R.id.recycler_view)
        placeholderView = findViewById(R.id.placeholder_view)
        retryButton = findViewById(R.id.retry_button)
        historyRecyclerView = findViewById(R.id.recycler_view_Istory)
        historyHeader = findViewById(R.id.Text_Istoriy)
        clearHistoryButton = findViewById(R.id.Ochistit_Istotiy)
        progressBar = findViewById(R.id.progressBar)

        // Инициализация SearchHistory
        searchHistory = SearchHistory(getSharedPreferences("AppPrefs", MODE_PRIVATE))

        // Настройка RecyclerView для результатов поиска
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(this, emptyList()) { track ->
            handleTrackClick(track)
        }
        recyclerView.adapter = trackAdapter

        // Настройка RecyclerView для истории
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(this, searchHistory.getHistory()) { track ->
            handleTrackClick(track)
        }
        historyRecyclerView.adapter = historyAdapter

        // Обработка кнопки "Назад"
        findViewById<ImageButton>(R.id.back_button2).setOnClickListener {
            finish()
        }

        // Управление видимостью кнопки "Очистить" и поведением при изменении текста
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                if (s.isNullOrEmpty()) {
                    clearButton.visibility = View.GONE
                    searchEditText.hint = getString(R.string.poisk)
                    // Очищаем результаты поиска и показываем историю
                    trackAdapter.updateTracks(emptyList())
                    recyclerView.visibility = View.GONE
                    hidePlaceholder()
                    showHistory()
                    progressBar.visibility = View.GONE
                } else {
                    clearButton.visibility = View.VISIBLE
                    searchEditText.hint = null
                    hideHistory()

                    // Сбрасываем предыдущий запланированный запрос
                    searchHandler.removeCallbacks(searchRunnable)
                    // Планируем новый запрос через 2 секунды
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка нажатия на кнопку "Очистить"
        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard(searchEditText)
            // Остальная логика обрабатывается в TextWatcher
        }

        // Скрытие клавиатуры при клике вне поля ввода
        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val view = currentFocus
                if (view is EditText) {
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)
                    if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        hideKeyboard(view)
                        view.clearFocus()
                    }
                }
            }
            false
        }

        // Обработка нажатия кнопки "Done" на клавиатуре
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    hideKeyboard(searchEditText)
                }
                true
            } else {
                false
            }
        }

        // Обработка нажатия на кнопку "Обновить"
        retryButton.setOnClickListener {
            searchQuery?.let { query ->
                performSearch(query)
            }
        }

        // Обработка нажатия на кнопку "Очистить историю"
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            hideHistory()
        }

        // Восстановление состояния
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("SEARCH_QUERY")
            searchEditText.setText(searchQuery)
            if (!searchQuery.isNullOrEmpty()) {
                clearButton.visibility = View.VISIBLE
                searchEditText.hint = null
                performSearch(searchQuery!!)
            } else {
                clearButton.visibility = View.GONE
                searchEditText.hint = getString(R.string.poisk)
                showHistory()
            }
        }

        // Отслеживание фокуса на поле поиска
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                showHistory()
            }
        }

        // Инициализация начального состояния
        if (searchHistory.getHistory().isEmpty()) {
            hideHistory()
        }
    }

    private fun performSearch(query: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = itunesApiService.search(query)
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.results
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        if (tracks.isEmpty()) {
                            showPlaceholder(PlaceholderType.NO_RESULTS)
                        } else {
                            trackAdapter.updateTracks(tracks)
                            showContent()
                        }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        showPlaceholder(PlaceholderType.ERROR) }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    showPlaceholder(PlaceholderType.ERROR) }
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        placeholderView.visibility = View.GONE
        hideHistory()
    }

    private fun showPlaceholder(type: PlaceholderType) {
        recyclerView.visibility = View.GONE
        placeholderView.visibility = View.VISIBLE
        val placeholderImage = findViewById<ImageView>(R.id.placeholder_image)
        val placeholderText = findViewById<TextView>(R.id.placeholder_text)
        when (type) {
            PlaceholderType.NO_RESULTS -> {
                placeholderImage.setImageResource(R.drawable.light_mode)
                placeholderText.text = getString(R.string.no_results)
                retryButton.visibility = View.GONE
            }
            PlaceholderType.ERROR -> {
                placeholderImage.setImageResource(R.drawable.nointer)
                placeholderText.text = getString(R.string.server_error)
                retryButton.visibility = View.VISIBLE
            }
        }
    }

    private fun hidePlaceholder() {
        placeholderView.visibility = View.GONE
    }

    private fun showHistory() {
        val history = searchHistory.getHistory()
        if (history.isNotEmpty()) {
            historyHeader.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
            historyAdapter.updateTracks(history)
        } else {
            hideHistory()
        }
    }

    private fun hideHistory() {
        historyHeader.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun handleTrackClick(track: Track) {

        if (!isClickAllowed) {
            return
        }
        isClickAllowed = false
        clickHandler.postDelayed({
            isClickAllowed = true
        }, CLICK_DEBOUNCE_DELAY)


        val history = searchHistory.getHistory().toMutableList()
        val existingIndex = history.indexOfFirst { it.trackId == track.trackId }
        if (existingIndex != -1) {
            history.removeAt(existingIndex)
        }
        history.add(0, track)
        if (history.size > 10) {
            history.removeAt(history.size - 1)
        }
        searchHistory.saveHistory(history)
        historyAdapter.updateTracks(history)
        val intent = Intent(this, MediaActivity::class.java).apply {
            putExtra("TRACK_ID", track.trackId)
            putExtra("TRACK_NAME", track.trackName)
            putExtra("ARTIST_NAME", track.artistName)
            putExtra("ARTWORK_URL", track.artworkUrl100)
            putExtra("COLLECTION_NAME", track.collectionName)
            putExtra("RELEASE_DATE", track.releaseDate)
            putExtra("PRIMARY_GENRE", track.primaryGenreName)
            putExtra("COUNTRY", track.country)
            putExtra("TRACK_TIME_MILLIS", track.trackTimeMillis)
            putExtra("PREVIEW_URL", track.previewUrl)
        }
        startActivity(intent)
    }

    private fun createItunesApiService(): ItunesApiService {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApiService::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString("SEARCH_QUERY")
        searchEditText.setText(searchQuery)
        if (!searchQuery.isNullOrEmpty()) {
            clearButton.visibility = View.VISIBLE
            searchEditText.hint = null
            performSearch(searchQuery!!)
        } else {
            clearButton.visibility = View.GONE
            searchEditText.hint = getString(R.string.poisk)
            showHistory()
        }
    }
}

interface ItunesApiService {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("country") country: String = "US",
    ): Response<ItunesSearchResponse>
}

enum class PlaceholderType {
    NO_RESULTS, ERROR
}