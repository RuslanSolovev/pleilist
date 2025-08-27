package com.example.playlistmaker.presentation.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.presentation.viewmodel.CreatePlaylistViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreatePlaylistViewModel by viewModel()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            Log.d("CreatePlaylistFragment", "Image selected: $uri")
            viewModel.updateCoverImage(uri)
            binding.coverImageView.setImageURI(uri)
            binding.addCoverButton.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("CreatePlaylistFragment", "onCreateView")
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CreatePlaylistFragment", "onViewCreated")

        setupListeners()
        setupTextWatchers()
        observeViewModel()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("CreatePlaylistFragment", "System back button pressed")
                handleBackPress() // Вызываем ваш общий обработчик
            }
        })

    }

    private fun setupListeners() {
        Log.d("CreatePlaylistFragment", "Setting up listeners")

        binding.backButton.setOnClickListener {
            Log.d("CreatePlaylistFragment", "Back button clicked")
            handleBackPress()
        }

        binding.coverContainer.setOnClickListener {
            Log.d("CreatePlaylistFragment", "Cover container clicked")
            openImagePicker()
        }

        binding.createButton.setOnClickListener {
            Log.d("CreatePlaylistFragment", "Create button clicked")
            // ОБНОВЛЯЕМ ПОЛЯ ПЕРЕД СОЗДАНИЕМ
            updateFieldsFromUI()
            viewModel.createPlaylist(
                onSuccess = { playlistName ->
                    Log.d("CreatePlaylistFragment", "Playlist created successfully: $playlistName")
                    Toast.makeText(
                        requireContext(),
                        "Плейлист \"$playlistName\" создан",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                },
                onError = { errorMessage ->
                    Log.e("CreatePlaylistFragment", "Error creating playlist: $errorMessage")
                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun setupTextWatchers() {
        // TextWatcher для названия
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateName(s.toString())
            }
        })

        // TextWatcher для описания
        binding.descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateDescription(s.toString())
            }
        })
    }

    private fun updateFieldsFromUI() {
        // Принудительно обновляем поля из UI
        viewModel.updateName(binding.nameEditText.text.toString())
        viewModel.updateDescription(binding.descriptionEditText.text.toString())
    }

    private fun observeViewModel() {
        Log.d("CreatePlaylistFragment", "Observing ViewModel")
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                Log.d("CreatePlaylistFragment", "UI State updated, button enabled: ${state.isCreateButtonEnabled}")
                binding.createButton.isEnabled = state.isCreateButtonEnabled

                // Синхронизируем UI с состоянием (на случай если состояние восстановилось)
                if (binding.nameEditText.text.toString() != state.name) {
                    binding.nameEditText.setText(state.name)
                }
                if (binding.descriptionEditText.text.toString() != state.description) {
                    binding.descriptionEditText.setText(state.description)
                }
            }
        }
    }

    private fun openImagePicker() {
        Log.d("CreatePlaylistFragment", "Opening image picker")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            pickImageLauncher.launch(intent)
        } else {
            Log.e("CreatePlaylistFragment", "Cannot resolve image picker intent")
            Toast.makeText(requireContext(), "Не удалось открыть галерею", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBackPress() {
        Log.d("CreatePlaylistFragment", "Handling back press")
        if (viewModel.hasUnsavedChanges()) {
            showExitConfirmationDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showExitConfirmationDialog() {
        Log.d("CreatePlaylistFragment", "Showing exit confirmation dialog")
        AlertDialog.Builder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохраненные данные будут потеряны")
            .setPositiveButton("Завершить") { _, _ ->
                Log.d("CreatePlaylistFragment", "User confirmed exit")
                findNavController().popBackStack()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                Log.d("CreatePlaylistFragment", "User cancelled exit")
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("CreatePlaylistFragment", "onDestroyView")
        _binding = null
    }
}