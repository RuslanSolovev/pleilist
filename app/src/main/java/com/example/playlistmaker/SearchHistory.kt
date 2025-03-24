package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10 // Максимальное количество треков в истории
    }

    // Получить историю поиска
    fun getHistory(): List<Track> {
        val historyJson = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (historyJson != null) {
            Gson().fromJson(historyJson, object : TypeToken<List<Track>>() {}.type)
        } else {
            emptyList()
        }
    }

    // Сохранить историю поиска
    fun saveHistory(history: List<Track>) {
        val updatedHistory = if (history.size > MAX_HISTORY_SIZE) {
            history.take(MAX_HISTORY_SIZE) // Оставляем только последние 10 треков
        } else {
            history
        }
        val historyJson = Gson().toJson(updatedHistory)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, historyJson).apply()
    }

    // Очистить историю поиска
    fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }
}