package com.example.playlistmaker.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.usecase.CreatePlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CreatePlaylistViewModel(
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistState())
    val uiState: StateFlow<CreatePlaylistState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        // Только проверка названия для активации кнопки
        _uiState.value = _uiState.value.copy(
            name = name,
            isCreateButtonEnabled = name.isNotBlank()
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateCoverImage(coverImageUri: Uri?) {
        _uiState.value = _uiState.value.copy(coverImageUri = coverImageUri)
    }

    fun createPlaylist(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        // ТОЛЬКО проверка названия
        if (state.name.isNotBlank()) {
            // Блокируем кнопку
            _uiState.value = _uiState.value.copy(isCreateButtonEnabled = false)

            viewModelScope.launch {
                try {
                    Log.d("CreatePlaylistVM", "Starting playlist creation")

                    // Копируем изображение
                    val coverImagePath = if (state.coverImageUri != null) {
                        copyCoverImageToInternalStorage(state.coverImageUri)
                    } else {
                        null
                    }
                    Log.d("CreatePlaylistVM", "Cover image path: $coverImagePath")

                    // Создаем объект Playlist
                    // Описание может быть null или пустой строкой - оба варианта допустимы
                    val descriptionToUse = state.description.takeIf { it.isNotBlank() }

                    val newPlaylist = Playlist(
                        name = state.name.trim(), // Убираем пробелы по краям
                        description = descriptionToUse, // Может быть null
                        coverImagePath = coverImagePath, // Может быть null
                        trackIds = emptyList(),
                        tracksCount = 0,
                        createdAt = System.currentTimeMillis()
                    )

                    Log.d("CreatePlaylistVM", "Playlist object created: $newPlaylist")

                    // Вызываем UseCase
                    createPlaylistUseCase(newPlaylist)

                    onSuccess(state.name.trim())
                } catch (e: Exception) {
                    Log.e("CreatePlaylistVM", "Error creating playlist", e)
                    // Разблокируем кнопку при ошибке
                    _uiState.value = _uiState.value.copy(isCreateButtonEnabled = true)
                    onError(e.message ?: "Неизвестная ошибка при создании плейлиста")
                }
            }
        } else {
            onError("Введите название плейлиста")
        }
    }

    fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value
        return state.name.isNotBlank() ||
                state.description.isNotBlank() ||
                state.coverImageUri != null
    }

    private suspend fun copyCoverImageToInternalStorage(uri: Uri?): String? {
        if (uri == null) return null

        return try {
            Log.d("CreatePlaylistVM", "Copying cover image from URI: $uri")

            val fileName = "playlist_cover_${System.currentTimeMillis()}.jpg"
            val coverDir = File(context.filesDir, "playlist_covers")
            if (!coverDir.exists()) {
                coverDir.mkdirs()
            }
            val destinationFile = File(coverDir, fileName)
            Log.d("CreatePlaylistVM", "Destination file: ${destinationFile.absolutePath}")

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("CreatePlaylistVM", "Could not open input stream for URI: $uri")
                return null
            }

            val outputStream: OutputStream = FileOutputStream(destinationFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("CreatePlaylistVM", "Cover image copied successfully")
            destinationFile.absolutePath
        } catch (e: IOException) {
            Log.e("CreatePlaylistVM", "IO Error copying cover image", e)
            null
        } catch (e: SecurityException) {
            Log.e("CreatePlaylistVM", "Security error copying cover image", e)
            null
        } catch (e: Exception) {
            Log.e("CreatePlaylistVM", "Unexpected error copying cover image", e)
            null
        }
    }
}

data class CreatePlaylistState(
    val name: String = "",
    val description: String = "",
    val coverImageUri: Uri? = null,
    val isCreateButtonEnabled: Boolean = false
)