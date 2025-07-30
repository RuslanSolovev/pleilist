package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val _state = MutableStateFlow<SearchState>(SearchState.Default)
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var currentQuery = ""
    private var currentResults = emptyList<Track>()

    private val _searchQuery = MutableStateFlow("")
    private val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        setupSearchDebounce()
    }

    private fun setupSearchDebounce() {
        searchQuery
            .debounce(2000)
            .onEach { query ->
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    showHistory()
                }
            }
            .catch { e -> _state.update { SearchState.Error } }
            .launchIn(viewModelScope)
    }

    fun searchDebounced(query: String) {
        currentQuery = query
        _searchQuery.value = query
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { SearchState.Loading }
            searchInteractor.search(query)
                .collect { results ->
                    currentResults = results
                    _state.update {
                        if (results.isEmpty()) SearchState.NoResults
                        else SearchState.Success(results)
                    }
                }
        }
    }

    private fun showHistory() {
        viewModelScope.launch {
            val history = historyInteractor.getHistory()
            _state.update { SearchState.ShowHistory(history) }
        }
    }

    fun handleTrackClick(track: Track) {
        viewModelScope.launch {
            historyInteractor.saveTrack(track)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyInteractor.clearHistory()
            _state.update { SearchState.ShowHistory(emptyList()) }
        }
    }

    fun restoreState() {
        if (currentQuery.isEmpty()) {
            showHistory()
        } else {
            if (currentResults.isNotEmpty()) {
                _state.update { SearchState.Success(currentResults) }
            } else {
                performSearch(currentQuery)
            }
        }
    }

    fun retrySearch() {
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
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