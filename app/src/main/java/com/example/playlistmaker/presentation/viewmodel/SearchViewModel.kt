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
    private var currentQuery: String = ""
    private var currentResults: List<Track> = emptyList()
    private var isHistoryShowing: Boolean = false

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState> = _state

    fun searchDebounced(query: String) {
        currentQuery = query
        searchJob?.cancel()

        if (query.isEmpty()) {
            showHistoryIfNeeded()
            return
        }

        isHistoryShowing = false
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    fun retry() {
        performSearch(currentQuery)
    }

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

    fun restoreState() {
        if (currentQuery.isEmpty()) {
            showHistoryIfNeeded()
        } else {
            if (currentResults.isNotEmpty()) {
                _state.value = SearchState.Success(currentResults)
            } else {
                performSearch(currentQuery)
            }
        }
    }

    private fun showHistoryIfNeeded() {
        if (currentQuery.isEmpty()) {
            isHistoryShowing = true
            loadHistory()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val historyList = historyInteractor.getHistory()
            if (isHistoryShowing) {
                _state.postValue(SearchState.ShowHistory(historyList))
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyInteractor.clearHistory()
            if (isHistoryShowing) {
                _state.postValue(SearchState.ShowHistory(emptyList()))
            }
        }
    }

    fun saveTrackToHistory(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            historyInteractor.saveTrack(track)
            // Не обновляем UI здесь, чтобы избежать мигания
        }
    }
}

sealed class SearchState {
    object Loading : SearchState()
    object NoResults : SearchState()
    object Error : SearchState()
    data class Success(val tracks: List<Track>) : SearchState()
    data class ShowHistory(val history: List<Track>) : SearchState()
}