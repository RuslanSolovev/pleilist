package com.example.playlistmaker.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.usecase.CreatePlaylistUseCase
import com.example.playlistmaker.domain.usecase.GetPlaylistByIdUseCase
import com.example.playlistmaker.domain.usecase.UpdatePlaylistUseCase
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    createPlaylistUseCase: CreatePlaylistUseCase,
    context: Context
) : CreatePlaylistViewModel(createPlaylistUseCase, context) {

    private var originalPlaylist: Playlist? = null
    private var originalCoverImagePath: String? = null

    // Метод для загрузки данных плейлиста для редактирования
    fun loadPlaylistForEditing(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = getPlaylistByIdUseCase(playlistId)
                if (playlist != null) {
                    originalPlaylist = playlist
                    originalCoverImagePath = playlist.coverImagePath // Сохраняем оригинальный путь

                    // Обновляем состояние ViewModel данными плейлиста
                    _uiState.value = CreatePlaylistState(
                        name = playlist.name,
                        description = playlist.description ?: "",
                        coverImageUri = playlist.coverImagePath?.let { Uri.parse(it) },
                        isCreateButtonEnabled = playlist.name.isNotBlank()
                    )
                    Log.d("EditPlaylistVM", "Loaded playlist data for ID: $playlistId")
                } else {
                    Log.e("EditPlaylistVM", "Playlist with ID $playlistId not found.")
                }
            } catch (e: Exception) {
                Log.e("EditPlaylistVM", "Error loading playlist for editing", e)
            }
        }
    }

    // Переопределяем метод сохранения для редактирования
    override fun createPlaylist(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        val playlistToEdit = originalPlaylist
        if (state.name.isNotBlank() && playlistToEdit != null) {
            _uiState.value = _uiState.value.copy(isCreateButtonEnabled = false)

            viewModelScope.launch {
                try {
                    Log.d("EditPlaylistVM", "Starting playlist update")

                    var coverImagePath: String? = originalCoverImagePath

                    // Если выбрана новая обложка, копируем её
                    if (state.coverImageUri != null) {
                        val newCoverImagePath = copyCoverImageToInternalStorage(state.coverImageUri)
                        if (newCoverImagePath != null) {
                            coverImagePath = newCoverImagePath
                            Log.d("EditPlaylistVM", "New cover image copied: $coverImagePath")
                        }
                    } else if (state.coverImageUri == null && originalCoverImagePath != null) {
                        // Если обложка была удалена (пользователь выбрал null)
                        coverImagePath = null
                        Log.d("EditPlaylistVM", "Cover image removed")
                    }

                    val descriptionToUse = state.description.takeIf { it.isNotBlank() }

                    // Создаём обновлённый объект плейлиста
                    val updatedPlaylist = playlistToEdit.copy(
                        name = state.name.trim(),
                        description = descriptionToUse,
                        coverImagePath = coverImagePath
                    )
                    Log.d("EditPlaylistVM", "Playlist object updated: $updatedPlaylist")

                    // Вызываем UseCase для обновления
                    updatePlaylistUseCase(updatedPlaylist)

                    onSuccess(state.name.trim())
                } catch (e: Exception) {
                    Log.e("EditPlaylistVM", "Error updating playlist", e)
                    _uiState.value = _uiState.value.copy(isCreateButtonEnabled = true)
                    onError(e.message ?: "Неизвестная ошибка при обновлении плейлиста")
                }
            }
        } else if (playlistToEdit == null) {
            onError("Плейлист для редактирования не найден")
        } else {
            onError("Введите название плейлиста")
        }
    }

    // Переопределяем проверку несохранённых изменений
    override fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value
        val original = originalPlaylist ?: return false

        val currentCoverUriString = state.coverImageUri?.toString()
        val originalCoverUriString = original.coverImagePath?.let { Uri.parse(it).toString() }

        return state.name.trim() != original.name ||
                state.description != (original.description ?: "") ||
                currentCoverUriString != originalCoverUriString
    }

    // Метод для удаления обложки
    fun removeCoverImage() {
        _uiState.value = _uiState.value.copy(coverImageUri = null)
    }

    fun savePlaylist(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        createPlaylist(onSuccess, onError)
    }
}