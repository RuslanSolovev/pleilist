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
import com.example.playlistmaker.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        setupWindowInsets()
        setupThemeSwitch()
        setupClickListeners()
        observeViewModelEvents()
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
        themeSwitch.isChecked = viewModel.isDarkThemeEnabled()

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTheme(isChecked)
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
            viewModel.onShareClicked()
        }

        findViewById<LinearLayout>(R.id.support_button).setOnClickListener {
            viewModel.onSupportClicked()
        }

        findViewById<LinearLayout>(R.id.terms_button).setOnClickListener {
            viewModel.onTermsClicked()
        }
    }

    private fun observeViewModelEvents() {
        viewModel.shareEvent.observe(this) { shareText ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.podel_cherez)))
        }

        viewModel.supportEvent.observe(this) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@example.com")
                putExtra(Intent.EXTRA_SUBJECT, "Playlist Maker Support")
                putExtra(Intent.EXTRA_TEXT, "Describe your issue here...")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Нет почтового клиента", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.termsEvent.observe(this) { url ->
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "Cannot open browser", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
