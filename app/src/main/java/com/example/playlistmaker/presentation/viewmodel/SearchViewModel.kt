package com.example.playlistmaker.presentation

import androidx.lifecycle.*
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private var searchJob: Job? = null
    private var currentQuery: String? = null
    private var currentResults: List<Track> = emptyList()

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState> = _state

    private val _history = MutableLiveData<List<Track>>(emptyList())
    val history: LiveData<List<Track>> = _history

    /**
     * Запускает поиск с дебаунсом
     */
    fun searchDebounced(query: String) {
        currentQuery = query
        searchJob?.cancel()
        if (query.isEmpty()) {
            _state.postValue(SearchState.ShowHistory)
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    /**
     * Повтор поиска текущего запроса
     */
    fun retry() {
        currentQuery?.let {
            performSearch(it)
        }
    }

    /**
     * Выполнение поиска и сохранение результатов
     */
    private fun performSearch(query: String) {
        _state.postValue(SearchState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = searchInteractor.execute(query)
                currentResults = result
                if (result.isEmpty()) {
                    _state.postValue(SearchState.NoResults)
                } else {
                    _state.postValue(SearchState.Success(result))
                }
            } catch (e: Exception) {
                _state.postValue(SearchState.Error)
            }
        }
    }

    /**
     * Восстановление предыдущего состояния поиска при возврате на экран
     */
    fun restoreSearch() {
        if (currentQuery.isNullOrEmpty()) {
            loadHistory()
            _state.value = SearchState.ShowHistory // Используем value вместо postValue, так как в главном потоке
        } else {
            if (currentResults.isNotEmpty()) {
                _state.value = SearchState.Success(currentResults)
            } else {
                // Если есть запрос, но нет результатов - показываем пустое состояние
                _state.value = SearchState.Empty
            }
        }
    }

    /**
     * Загрузка истории поиска
     */
    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val historyList = historyInteractor.getHistory()
            _history.postValue(historyList)
        }
    }

    /**
     * Очистка истории поиска
     */
    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyInteractor.clearHistory()
            _history.postValue(emptyList())
        }
    }

    /**
     * Сохранение трека в историю: удаление дубликатов и лимит 10
     */
    fun saveTrackToHistory(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            historyInteractor.saveTrack(track)
            val updatedHistory = historyInteractor.getHistory()
            _history.postValue(updatedHistory)
        }
    }
}

sealed class SearchState {

    object ShowHistory : SearchState()
    object Loading : SearchState()
    object NoResults : SearchState()
    object Error : SearchState()
    object Empty : SearchState()
    data class Success(val tracks: List<Track>) : SearchState()
}
