package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavoriteInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteInteractor: FavoriteInteractor
) : ViewModel() {

    private val _favorites = MutableLiveData<List<Track>>()
    val favorites: LiveData<List<Track>> = _favorites

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            favoriteInteractor.getFavorites().collect { tracks ->
                _favorites.postValue(tracks.sortedByDescending { it.trackId })
            }
        }
    }
}