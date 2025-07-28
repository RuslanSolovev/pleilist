package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private var searchJob: Job? = null
    private var clickJob: Job? = null
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

    fun handleTrackClickWithDebounce(track: Track) {
        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            saveTrackToHistory(track)
        }
    }

    private fun performSearch(query: String) {
        _state.value = SearchState.Loading
        viewModelScope.launch {
            searchInteractor.execute(query)
                .catch { e ->
                    _state.postValue(SearchState.Error)
                }
                .collect { result ->
                    currentResults = result
                    _state.postValue(if (result.isEmpty()) {
                        SearchState.NoResults
                    } else {
                        SearchState.Success(result)
                    })
                }
        }
    }

    fun retry() {
        performSearch(currentQuery)
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
        }
    }
}

sealed class SearchState {
    object Default : SearchState()
    object Loading : SearchState()
    object NoResults : SearchState()
    object Error : SearchState()
    data class Success(val tracks: List<Track>) : SearchState()
    data class ShowHistory(val history: List<Track>) : SearchState()
}