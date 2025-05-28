package com.example.playlistmaker.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val themeSwitch = findViewById<Switch>(R.id.theme_switch)

        // Подписка на LiveData — делаем Intent при получении события
        viewModel.shareAppEvent.observe(this) { shareText ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.podel_cherez)))
        }

        // Подписка на изменение темы
        viewModel.darkThemeEnabled.observe(this) { isDark ->
            if (themeSwitch.isChecked != isDark) {
                themeSwitch.isChecked = isDark
            }
            // Применяем тему в приложении
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Обработка переключения тумблера темы
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.darkThemeEnabled.value) {
                viewModel.toggleTheme()
            }
        }

        // Назад
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Поделиться — ViewModel обрабатывает
        findViewById<LinearLayout>(R.id.share_button).setOnClickListener {
            viewModel.onShareClicked()
        }

        // Поддержка
        findViewById<LinearLayout>(R.id.support_button).setOnClickListener {
            sendEmail()
        }

        // Условия использования
        findViewById<LinearLayout>(R.id.terms_button).setOnClickListener {
            openTermsAndConditions()
        }
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:solovevrus1993@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.soobchenie_razrab))
        }
        startActivity(emailIntent)
    }

    private fun openTermsAndConditions() {
        val termsUrl = "https://yandex.ru/legal/practicum_offer/"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl)))
    }
}
