package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Настройка отступов для полного использования экрана
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Загрузка сохраненного состояния темы
        loadTheme()

        // Настройка переключателя темы
        themeSwitch = findViewById(R.id.theme_switch)
        themeSwitch.isChecked = isDarkModeEnabled()

        // Обработка изменения состояния переключателя
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveTheme(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveTheme(false)
            }
            recreate() // Пересоздание активности для применения новой темы
        }

        // Обработка кнопки "Назад"
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish() // Закрываем активити
        }

        // Обработка кнопки "Поделиться приложением"
        findViewById<LinearLayout>(R.id.share_button).setOnClickListener {
            shareApp()
        }

        // Обработка кнопки "Написать в поддержку"
        findViewById<LinearLayout>(R.id.support_button).setOnClickListener {
            sendEmail()
        }

        // Обработка кнопки "Пользовательское соглашение"
        findViewById<LinearLayout>(R.id.terms_button).setOnClickListener {
            openTermsAndConditions()
        }
    }

    // Метод для шаринга приложения
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.haring)
            )
        }
        val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.podel_cherez))
        try {
            startActivity(chooserIntent)
        } catch (e: Exception) {
            showMessage(getString(R.string.net_haring))
        }
    }

    // Метод для открытия пользовательского соглашения
    private fun openTermsAndConditions() {
        val termsUrl = "https://yandex.ru/legal/practicum_offer/"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(browserIntent)
        } catch (e: Exception) {
            showMessage(getString(R.string.net_brauzer))
        }
    }

    // Метод для отправки письма
    private fun sendEmail() {
        val recipient = "solovevrus1993@gmail.com"
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Указывает, что это email
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.soobchenie_razrab))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.spasibo_razrab))
        }
        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            showMessage(getString(R.string.net_pozta))
        }
    }

    // Метод для показа сообщений
    private fun showMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    // Сохранение состояния темы
    private fun saveTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("IS_DARK_MODE", isDarkMode).apply()
    }

    // Загрузка сохраненного состояния темы
    private fun loadTheme() {
        val isDarkMode = isDarkModeEnabled()
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Проверка, включена ли тёмная тема
    private fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("IS_DARK_MODE", false)
    }
}