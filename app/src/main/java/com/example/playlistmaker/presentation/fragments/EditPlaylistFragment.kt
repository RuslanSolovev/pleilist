package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.presentation.viewmodel.EditPlaylistViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    // Используем свою ViewModel
    private val editViewModel: EditPlaylistViewModel by viewModel()

    // Переопределяем лаунчер для выбора изображения, чтобы он обновлял editViewModel
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
        private const val TAG = "EditPlaylistFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        hideActionBar()
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        // Инициализируем pickImageLauncher ПОСЛЕ super.onViewCreated, чтобы он использовал editViewModel
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d(TAG, "Image selected in EditPlaylistFragment: $uri")
            uri?.let {
                // Обновляем состояние editViewModel, а не viewModel родителя
                editViewModel.updateCoverImage(it)
                binding.coverImageView.setImageURI(it)
                // Убедимся, что кнопка активна, если название не пустое
                // editViewModel.updateName(binding.nameEditText.text.toString()) // Обычно TextWatcher это делает
            }
        }

        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: run {
            Log.e(TAG, "Playlist ID not found in arguments")
            Toast.makeText(context, "Ошибка: ID плейлиста не найден", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }
        Log.d(TAG, "Received playlistId: $playlistId")

        // Загружаем данные плейлиста в ViewModel
        editViewModel.loadPlaylistForEditing(playlistId)

        // Подписываемся на состояние своей ViewModel
        observeEditViewModel()

        // ПРАВИЛЬНОЕ УСТАНОВЛЕНИЕ ЗАГОЛОВКА
        binding.titleText.text = getString(R.string.edit_playlist_title) // Установите текст в TextView заголовка

        // Меняем текст кнопки
        binding.createButton.text = getString(R.string.save_button_text)

        // Переопределяем OnBackPressedCallback
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun observeEditViewModel() {
        Log.d(TAG, "Observing EditViewModel")
        lifecycleScope.launch {
            editViewModel.uiState.collect { state ->
                Log.d(TAG, "Edit UI State updated, button enabled: ${state.isCreateButtonEnabled}")
                binding.createButton.isEnabled = state.isCreateButtonEnabled

                // Синхронизация названия и описания
                if (binding.nameEditText.text.toString() != state.name) {
                    binding.nameEditText.setText(state.name)
                }
                if (binding.descriptionEditText.text.toString() != state.description) {
                    binding.descriptionEditText.setText(state.description)
                }

                // Обновление обложки
                // Проверяем, изменился ли URI в состоянии VM по сравнению с тем, что отображается
                val currentImageTag = binding.coverImageView.tag?.toString()
                val newStateImageUriString = state.coverImageUri?.toString()

                if (newStateImageUriString != currentImageTag) {
                    if (state.coverImageUri != null) {
                        binding.coverImageView.setImageURI(state.coverImageUri)
                    } else {
                        binding.coverImageView.setImageResource(R.drawable.placeholder_vector)
                    }
                    // Сохраняем текущий URI как tag для последующего сравнения
                    binding.coverImageView.tag = newStateImageUriString
                }

                // Обновление цветов полей ввода
                updateInputLayoutColors(
                    binding.nameInputLayout,
                    state.name.isNotEmpty(),
                    binding.nameEditText.hasFocus()
                )
                updateInputLayoutColors(
                    binding.descriptionInputLayout,
                    state.description.isNotEmpty(),
                    binding.descriptionEditText.hasFocus()
                )

                // Установка заголовка (на всякий случай, хотя он уже установлен)
                binding.titleText.text = getString(R.string.edit_playlist_title)
            }
        }
    }

    // Переопределяем обработчик кнопки создания/сохранения
    override fun setupListeners() {
        // Сначала вызываем родительский метод, чтобы установить общие слушатели (back, cover click)
        super.setupListeners()
        Log.d(TAG, "Setting up specific listeners for editing")

        // Переопределяем обработчик клика на контейнер обложки, чтобы использовать наш pickImageLauncher
        binding.coverContainer.setOnClickListener {
            Log.d(TAG, "Cover container clicked in EditPlaylistFragment")
            openImagePicker()
        }

        // Переопределяем обработчик кнопки "Создать" (которая теперь "Сохранить")
        binding.createButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            updateFieldsFromUI() // Обновляем состояние VM из UI
            editViewModel.savePlaylist(
                onSuccess = { playlistName ->
                    Log.d(TAG, "Playlist updated successfully: $playlistName")
                    Toast.makeText(
                        requireContext(),
                        "Плейлист \"$playlistName\" обновлён",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack() // Возвращаемся на экран деталей плейлиста
                },
                onError = { errorMessage ->
                    Log.e(TAG, "Error updating playlist: $errorMessage")
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // Переопределяем openImagePicker, чтобы использовать наш pickImageLauncher
    override fun openImagePicker() {
        Log.d(TAG, "Opening image picker in EditPlaylistFragment")
        pickImageLauncher.launch("image/*")
    }

    override fun updateFieldsFromUI() {
        Log.d(TAG, "Updating fields from UI for EditViewModel")
        // Принудительно обновляем поля из UI для editViewModel
        editViewModel.updateName(binding.nameEditText.text.toString())
        editViewModel.updateDescription(binding.descriptionEditText.text.toString())
        // Обложка обновляется через pickImageLauncher или observeEditViewModel.
    }

    // Переопределяем обработчик системной кнопки "Назад"
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.d(TAG, "System back button pressed (Edit mode)")
            handleBackPressForEdit() // Используем свою логику
        }
    }

    // Своя логика обработки кнопки "Назад" для редактирования
    private fun handleBackPressForEdit() {
        Log.d(TAG, "Handling back press for edit")
        if (editViewModel.hasUnsavedChanges()) {
            showExitConfirmationDialog()
        } else {
            findNavController().popBackStack() // Просто закрываем экран
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Показываем ActionBar обратно при выходе из фрагмента (если скрывался)
        showActionBar()
        Log.d(TAG, "onDestroyView")
        // _binding = null // Уже делается в родителе
    }

    // --- Методы, которые могут потребоваться, если не полностью переопределяются ---
    // hideActionBar, showActionBar, setupTextWatchers, updateInputLayoutColors,
    // showExitConfirmationDialog - можно использовать из родителя
    // или переопределить при необходимости.
}