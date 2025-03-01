package com.example.playlistmaker

import TrackAdapter
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    // Глобальная переменная для хранения текста поискового запроса
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Обработка кнопки "Назад"
        findViewById<ImageButton>(R.id.back_button2).setOnClickListener {
            finish() // Закрываем активити
        }

        // Инициализация EditText и кнопки "Очистить"
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.clear_button)

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Создаем адаптер для списка треков
        val tracks = MockData.createMockTracks()
        trackAdapter = TrackAdapter(this, tracks)
        recyclerView.adapter = trackAdapter

        // Управление видимостью кнопки "Очистить" и поведением подсказки
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Обновляем глобальную переменную при изменении текста
                searchQuery = s.toString()

                if (s.isNullOrEmpty()) {
                    // Если текст пустой, скрываем кнопку "Очистить" и показываем подсказку
                    clearButton.visibility = View.GONE
                    searchEditText.hint = getString(R.string.poisk)

                    // Обновляем список треков на основе пустого запроса
                    updateTrackList("")
                } else {
                    // Если есть текст, показываем кнопку "Очистить" и скрываем подсказку
                    clearButton.visibility = View.VISIBLE
                    searchEditText.hint = null

                    // Обновляем список треков на основе текущего запроса
                    updateTrackList(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка нажатия на кнопку "Очистить"
        clearButton.setOnClickListener {
            searchEditText.setText("") // Очищаем текст
            hideKeyboard(searchEditText) // Скрываем клавиатуру

            // Обновляем список треков на основе пустого запроса
            updateTrackList("")
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

        // Восстанавливаем состояние при запуске активности
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("SEARCH_QUERY")
            searchEditText.setText(searchQuery) // Устанавливаем сохраненный текст
            if (!searchQuery.isNullOrEmpty()) {
                clearButton.visibility = View.VISIBLE // Показываем кнопку "Очистить"
                searchEditText.hint = null // Скрываем подсказку

                // Обновляем список треков на основе сохраненного запроса
                updateTrackList(searchQuery!!)
            } else {
                clearButton.visibility = View.GONE // Скрываем кнопку "Очистить"
                searchEditText.hint = getString(R.string.poisk) // Показываем подсказку

                // Обновляем список треков на основе пустого запроса
                updateTrackList("")
            }
        }
    }

    // Метод для скрытия клавиатуры
    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Переопределение метода для сохранения состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", searchQuery) // Сохраняем текст поискового запроса
    }

    // Переопределение метода для восстановления состояния
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString("SEARCH_QUERY") // Восстанавливаем текст
        searchEditText.setText(searchQuery) // Устанавливаем текст в EditText
        if (!searchQuery.isNullOrEmpty()) {
            clearButton.visibility = View.VISIBLE // Показываем кнопку "Очистить"
            searchEditText.hint = null // Скрываем подсказку

            // Обновляем список треков на основе восстановленного запроса
            updateTrackList(searchQuery!!)
        } else {
            clearButton.visibility = View.GONE // Скрываем кнопку "Очистить"
            searchEditText.hint = getString(R.string.poisk) // Показываем подсказку

            // Обновляем список треков на основе пустого запроса
            updateTrackList("")
        }
    }

    // Метод для фильтрации списка треков
    private fun updateTrackList(query: String) {
        val filteredTracks = MockData.createMockTracks().filter { track ->
            track.trackName.lowercase().contains(query.lowercase()) ||
                    track.artistName.lowercase().contains(query.lowercase())
        }
        trackAdapter.updateTracks(filteredTracks)
    }
}