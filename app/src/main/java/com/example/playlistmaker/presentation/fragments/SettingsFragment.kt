package com.example.playlistmaker.presentation.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.SettingsEvent
import com.example.playlistmaker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSwitch(view)
        setupClickListeners(view)
        observeEvents()
    }

    private fun setupThemeSwitch(view: View) {
        val themeSwitch = view.findViewById<Switch>(R.id.theme_switch)
        themeSwitch.isChecked = viewModel.getCurrentTheme()

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTheme(isChecked)
            applyTheme(isChecked)
        }
    }

    private fun applyTheme(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupClickListeners(view: View) {
        // Убираем кнопку назад
        view.findViewById<ImageButton>(R.id.back_button).visibility = View.GONE

        view.findViewById<LinearLayout>(R.id.share_button).setOnClickListener {
            viewModel.shareApp()
        }

        view.findViewById<LinearLayout>(R.id.support_button).setOnClickListener {
            viewModel.contactSupport()
        }

        view.findViewById<LinearLayout>(R.id.terms_button).setOnClickListener {
            viewModel.openTerms()
        }
    }

    private fun observeEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is SettingsEvent.Share -> handleShare(event.text)
                is SettingsEvent.Support -> handleSupport(event.intent, event.errorMessage)
                is SettingsEvent.Terms -> handleTerms(event.url)
            }
        }
    }

    private fun handleShare(text: String) {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }.let { startActivity(Intent.createChooser(it, getString(R.string.share_via))) }
    }

    private fun handleSupport(intent: Intent, errorMessage: String) {
        try {
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleTerms(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось открыть браузер", Toast.LENGTH_SHORT).show()
        }
    }
}