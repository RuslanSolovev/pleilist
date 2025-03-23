package com.example.playlistmaker

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        // Инициализация SearchHistory
        searchHistory = SearchHistory(getSharedPreferences("AppPrefs", MODE_PRIVATE))

        // Настройка RecyclerView для результатов поиска
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(this, emptyList()) { track ->
            searchHistory.addToHistory(track) // Добавляем трек в историю
            Toast.makeText(this, "Трек \"${track.trackName}\" добавлен в историю", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = trackAdapter

        // Настройка RecyclerView для истории
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(this, searchHistory.getHistory()) { track ->
            val query = track.trackName ?: ""
            performSearch(query)
        }
        historyRecyclerView.adapter = historyAdapter

        // Обработка кнопки "Назад"
        findViewById<ImageButton>(R.id.back_button2).setOnClickListener {
            finish()
        }

        // Управление видимостью кнопки "Очистить" и поведением подсказки
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                if (s.isNullOrEmpty()) {
                    clearButton.visibility = View.GONE
                    searchEditText.hint = getString(R.string.poisk)
                    showHistory()
                } else {
                    clearButton.visibility = View.VISIBLE
                    searchEditText.hint = null
                    hideHistory()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка нажатия на кнопку "Очистить"
        clearButton.setOnClickListener {
            searchEditText.setText("") // Очищаем поле ввода
            hideKeyboard(searchEditText) // Скрываем клавиатуру
            trackAdapter.updateTracks(emptyList()) // Очищаем адаптер треков
            recyclerView.visibility = View.GONE // Скрываем RecyclerView с результатами поиска
            showHistory() // Показываем историю
            hidePlaceholder() // Скрываем заглушку
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
                        hideHistory()
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
                    hideKeyboard(searchEditText) // Скрываем клавиатуру
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
            } else {
                hideHistory()
            }
        }

        // Инициализация начального состояния
        if (searchHistory.getHistory().isEmpty()) {
            hideHistory() // Скрываем историю, если она пуста
        }
    }

    // Метод для выполнения поискового запроса
    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {
                val response = itunesApiService.search(query)
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()!!.results
                    if (tracks.isEmpty()) {
                        showPlaceholder(PlaceholderType.NO_RESULTS)
                    } else {
                        trackAdapter.updateTracks(tracks)
                        showContent()
                    }
                } else {
                    showPlaceholder(PlaceholderType.ERROR)
                }
            } catch (e: Exception) {
                showPlaceholder(PlaceholderType.ERROR)
            }
        }
    }

    // Метод для скрытия клавиатуры
    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Отображение контента
    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        placeholderView.visibility = View.GONE
        hideHistory()
    }

    // Отображение заглушки
    private fun showPlaceholder(type: PlaceholderType) {
        recyclerView.visibility = View.GONE
        placeholderView.visibility = View.VISIBLE
        val placeholderImage = findViewById<ImageView>(R.id.placeholder_image)
        val placeholderText = findViewById<TextView>(R.id.placeholder_text)
        val retryButton = findViewById<View>(R.id.retry_button)

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

    // Скрытие заглушки
    private fun hidePlaceholder() {
        placeholderView.visibility = View.GONE
    }

    // Показать историю поиска
    private fun showHistory() {
        val history = searchHistory.getHistory()
        if (history.isNotEmpty()) {
            historyHeader.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
            historyAdapter.updateTracks(history)
        } else {
            hideHistory() // Скрываем историю, если она пуста
        }
    }

    // Скрыть историю поиска
    private fun hideHistory() {
        historyHeader.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    // Создание Retrofit сервиса
    private fun createItunesApiService(): ItunesApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ItunesApiService::class.java)
    }

    // Сохранение состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", searchQuery)
    }

    // Восстановление состояния
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

// Интерфейс для работы с iTunes API
interface ItunesApiService {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): Response<ItunesSearchResponse>
}

// Перечисление типов заглушек
enum class PlaceholderType {
    NO_RESULTS, ERROR
}