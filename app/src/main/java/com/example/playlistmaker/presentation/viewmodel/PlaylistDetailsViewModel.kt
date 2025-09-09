package com.example.playlistmaker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.usecase.DeletePlaylistUseCase
import com.example.playlistmaker.domain.usecase.GetPlaylistByIdUseCase
import com.example.playlistmaker.domain.usecase.GetTracksForPlaylistUseCase
import com.example.playlistmaker.domain.usecase.RemoveTrackFromPlaylistUseCase // <<< Добавлен импорт
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlaylistDetailsViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val getTracksForPlaylistUseCase: GetTracksForPlaylistUseCase,
    // --- Добавлен UseCase для удаления ---
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
    // -------------------------------------
) : ViewModel() {

    private val _state = MutableStateFlow<PlaylistDetailsState>(PlaylistDetailsState.Loading)
    val state: StateFlow<PlaylistDetailsState> = _state.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _state.value = PlaylistDetailsState.Loading
                Log.d("PlaylistDetailsVM", "Loading playlist with ID: $playlistId")

                val playlist = getPlaylistByIdUseCase(playlistId)
                if (playlist == null) {
                    val errorMsg = "Playlist with ID $playlistId not found"
                    Log.e("PlaylistDetailsVM", errorMsg)
                    _state.value = PlaylistDetailsState.Error(errorMsg)
                    return@launch
                }
                Log.d("PlaylistDetailsVM", "Playlist loaded: ${playlist.name}")

                val tracks = getTracksForPlaylistUseCase(playlist.trackIds)
                Log.d("PlaylistDetailsVM", "Loaded ${tracks.size} tracks for playlist")

                val totalDurationMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
                val formattedDuration = formatDuration(totalDurationMillis)
                Log.d("PlaylistDetailsVM", "Total duration calculated: $formattedDuration")

                _state.value = PlaylistDetailsState.Content(
                    playlist = playlist,
                    tracks = tracks,
                    totalDuration = formattedDuration,
                    totalDurationMillis = totalDurationMillis
                )
                Log.d("PlaylistDetailsVM", "State set to Content")

            } catch (e: Exception) {
                val errorMsg = e.message ?: "An unknown error occurred while loading the playlist"
                Log.e("PlaylistDetailsVM", "Error occurred: $errorMsg", e)
                _state.value = PlaylistDetailsState.Error(errorMsg)
            }
        }
    }

    fun removeTrackFromPlaylist(trackId: Int, playlistId: Long) {
        Log.d("PlaylistDetailsVM", "removeTrackFromPlaylist called for trackId: $trackId, playlistId: $playlistId")
        viewModelScope.launch {
            try {
                // Выполняем удаление через UseCase
                removeTrackFromPlaylistUseCase(trackId, playlistId)
                Log.d("PlaylistDetailsVM", "Track removed successfully, reloading playlist data...")
                // После успешного удаления перезагружаем данные плейлиста,
                // чтобы обновить список треков и информацию (кол-во треков, длительность)
                loadPlaylist(playlistId)
            } catch (e: Exception) {
                val errorMsg = "Ошибка при удалении трека: ${e.message}"
                Log.e("PlaylistDetailsVM", errorMsg, e)

            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        Log.d("PlaylistDetailsVM", "deletePlaylist called for playlistId: $playlistId")
        viewModelScope.launch {
            try {
                // Выполняем удаление через UseCase
                deletePlaylistUseCase(playlistId)
                Log.d("PlaylistDetailsVM", "Playlist deleted successfully")
                // После успешного удаления можно отправить событие или просто завершить
                // В PlaylistDetailsFragment вызывается findNavController().popBackStack()
            } catch (e: Exception) {
                val errorMsg = "Ошибка при удалении плейлиста: ${e.message}"
                Log.e("PlaylistDetailsVM", errorMsg, e)
                // Можно эмитить ошибку в отдельный StateFlow для отображения пользователю
                // или просто залогировать и показать Toast во Fragment
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}




sealed class PlaylistDetailsState {
    object Loading : PlaylistDetailsState()
    data class Content(
        val playlist: Playlist,
        val tracks: List<Track>,
        val totalDuration: String,
        val totalDurationMillis: Long
    ) : PlaylistDetailsState()
    data class Error(val message: String) : PlaylistDetailsState()
}