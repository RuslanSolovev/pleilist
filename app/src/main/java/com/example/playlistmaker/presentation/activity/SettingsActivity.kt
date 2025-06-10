package com.example.playlistmaker.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.SettingsEvent
import com.example.playlistmaker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        setupWindowInsets()
        setupThemeSwitch()
        setupClickListeners()
        observeEvents()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupThemeSwitch() {
        val themeSwitch = findViewById<Switch>(R.id.theme_switch)
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

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.share_button).setOnClickListener {
            viewModel.shareApp()
        }

        findViewById<LinearLayout>(R.id.support_button).setOnClickListener {
            viewModel.contactSupport()
        }

        findViewById<LinearLayout>(R.id.terms_button).setOnClickListener {
            viewModel.openTerms()
        }
    }

    private fun observeEvents() {
        viewModel.event.observe(this) { event ->
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
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleTerms(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open browser", Toast.LENGTH_SHORT).show()
        }
    }
}