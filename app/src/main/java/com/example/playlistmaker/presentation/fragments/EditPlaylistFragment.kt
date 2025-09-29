package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.presentation.viewmodel.EditPlaylistViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    private val editViewModel: EditPlaylistViewModel by viewModel()
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

        // Переинициализируем pickImageLauncher для использования editViewModel
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d(TAG, "Image selected in EditPlaylistFragment: $uri")
            uri?.let {
                editViewModel.updateCoverImage(it)
                binding.coverImageView.setImageURI(it)
            }
        }

        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: run {
            Log.e(TAG, "Playlist ID not found in arguments")
            Toast.makeText(context, "Ошибка: ID плейлиста не найден", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }
        Log.d(TAG, "Received playlistId: $playlistId")

        editViewModel.loadPlaylistForEditing(playlistId)
        observeEditViewModel()

        binding.titleText.text = getString(R.string.edit_playlist_title)
        binding.createButton.text = getString(R.string.save_button_text)

        // ВАЖНО: Устанавливаем СВОИ слушатели, включая TextWatchers
        setupEditListeners()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun observeEditViewModel() {
        Log.d(TAG, "Observing EditViewModel")
        lifecycleScope.launch {
            editViewModel.uiState.collect { state ->
                Log.d(TAG, "Edit UI State updated, button enabled: ${state.isCreateButtonEnabled}")
                binding.createButton.isEnabled = state.isCreateButtonEnabled

                // Синхронизируем UI с состоянием VM, избегая зацикливания
                val currentNameInUI = binding.nameEditText.text.toString()
                val currentDescriptionInUI = binding.descriptionEditText.text.toString()

                if (currentNameInUI != state.name) {
                    binding.nameEditText.setText(state.name)
                }
                if (currentDescriptionInUI != state.description) {
                    binding.descriptionEditText.setText(state.description)
                }

                // Обновление обложки
                val currentImageTag = binding.coverImageView.tag?.toString()
                val newStateImageUriString = state.coverImageUri?.toString()
                if (newStateImageUriString != currentImageTag) {
                    if (state.coverImageUri != null) {
                        binding.coverImageView.setImageURI(state.coverImageUri)
                    } else {
                        binding.coverImageView.setImageResource(R.drawable.placeholder_vector)
                    }
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
            }
        }
    }

    // Полностью переопределяем установку слушателей для редактирования
    private fun setupEditListeners() {
        Log.d(TAG, "Setting up EDIT-specific listeners")

        // --- Обработчики кликов ---
        binding.backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked in Edit mode")
            handleBackPressForEdit()
        }

        binding.coverContainer.setOnClickListener {
            Log.d(TAG, "Cover container clicked in Edit mode")
            openImagePicker()
        }

        binding.createButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            // Принудительно обновляем VM из UI перед сохранением
            editViewModel.updateName(binding.nameEditText.text.toString())
            editViewModel.updateDescription(binding.descriptionEditText.text.toString())

            editViewModel.savePlaylist(
                onSuccess = { playlistName ->
                    Log.d(TAG, "Playlist updated successfully: $playlistName")
                    Toast.makeText(requireContext(), "Плейлист \"$playlistName\" обновлён", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = { errorMessage ->
                    Log.e(TAG, "Error updating playlist: $errorMessage")
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }
        // --------------------------

        // --- Устанавливаем СВИХ TextWatchers для editViewModel ---
        // Сначала удалим любые существующие слушатели (на случай, если super.setup вызывался)
        // Это не всегда надежно, но стоит попробовать
        try {
            binding.nameEditText.clearTextChangedListeners() // Не стандартный метод, может не быть
        } catch (e: Exception) {
            Log.d(TAG, "Could not clear text watchers for nameEditText")
        }
        try {
            binding.descriptionEditText.clearTextChangedListeners() // Не стандартный метод, может не быть
        } catch (e: Exception) {
            Log.d(TAG, "Could not clear text watchers for descriptionEditText")
        }

        // Добавляем новые слушатели, которые обновляют editViewModel
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Обновляем editViewModel при вводе
                editViewModel.updateName(s.toString())
                Log.d(TAG, "Name updated in EditVM: ${s.toString()}")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Обновляем editViewModel при вводе
                editViewModel.updateDescription(s.toString())
                Log.d(TAG, "Description updated in EditVM: ${s.toString()}")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // ------------------------------------------------------------
    }

    // Переопределяем openImagePicker, чтобы использовать наш pickImageLauncher
    override fun openImagePicker() {
        Log.d(TAG, "Opening image picker in EditPlaylistFragment")
        pickImageLauncher.launch("image/*")
    }

    // Переопределяем setupListeners, чтобы НЕ вызывать родительскую логику TextWatchers
    override fun setupListeners() {
        // Намеренно оставляем пустым или вызываем только базовую навигационную логику,
        // если она была в родительском setupListeners.
        // Вся специфическая логика редактирования находится в setupEditListeners.
        Log.d(TAG, "setupListeners overridden, calling setupEditListeners")
        setupEditListeners()
    }

    // Переопределяем setupTextWatchers, чтобы НЕ устанавливать родительские TextWatchers
    override fun setupTextWatchers() {
        // Намеренно оставляем пустым.
        // TextWatchers устанавливаются в setupEditListeners.
        Log.d(TAG, "setupTextWatchers overridden and skipped")
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.d(TAG, "System back button pressed (Edit mode)")
            handleBackPressForEdit()
        }
    }

    private fun handleBackPressForEdit() {
        Log.d(TAG, "Handling back press for edit")
        if (editViewModel.hasUnsavedChanges()) {
            showExitConfirmationDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showActionBar()
        Log.d(TAG, "onDestroyView")
    }
}

// Вспомогательное расширение для попытки очистки TextWatchers (необязательно, но может помочь)
private fun android.widget.EditText.clearTextChangedListeners() {
    // Этот метод не существует в стандартном Android SDK.
    // Это просто заглушка, чтобы показать идею.
    // Реальная очистка TextWatchers требует хранения ссылок на них.
    // В большинстве случаев достаточно просто не вызывать super.setupTextWatchers()
    // и установить свои слушатели.
    // Если возникают проблемы, можно попробовать рефлексию или другие методы,
    // но это сложнее и менее надежно.
}