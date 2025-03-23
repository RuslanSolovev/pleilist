package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private val historyKey = "SEARCH_HISTORY"

    // Получение истории из SharedPreferences
    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Track>>() {}.type)
        } else {
            emptyList()
        }
    }

    // Добавление трека в историю
    fun addToHistory(track: Track) {
        val currentHistory = getHistory().toMutableList()



        // Удаляем трек, если он уже есть в истории
        currentHistory.removeAll { it.trackId == track.trackId }
        // Добавляем трек в начало списка
        currentHistory.add(0, track)
        // Ограничиваем размер истории до 10 элементов
        if (currentHistory.size > 10) {
            currentHistory.removeLast()
        }
        // Сохраняем обновленную историю
        val updatedJson = gson.toJson(currentHistory)
        sharedPreferences.edit().putString(historyKey, updatedJson).apply()
    }

    // Очистка истории
    fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }
}