package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.usecase.GetAllPlaylistsUseCase
import com.example.playlistmaker.domain.usecase.DeletePlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isEmptyState = MutableStateFlow(true)
    val isEmptyState: StateFlow<Boolean> = _isEmptyState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            try {

                getAllPlaylistsUseCase().collect { playlists ->

                    _playlists.value = playlists
                    _isEmptyState.value = playlists.isEmpty()
                }
            } catch (e: Exception) {

                _playlists.value = emptyList()
                _isEmptyState.value = true
            }
        }
    }

    fun refreshPlaylists() {

        loadPlaylists()
    }


    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {

                deletePlaylistUseCase(playlistId)

                refreshPlaylists()
            } catch (e: Exception) {

                refreshPlaylists()
            }
        }
    }
}