package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val historyKey = "search_history"
    private val likesKey = "track_likes"
    private val gson = Gson()

    // Сохранение истории поиска
    fun saveHistory(tracks: List<Track>) {
        val trackJson = gson.toJson(tracks) // Преобразуем список треков в JSON
        sharedPreferences.edit().putString(historyKey, trackJson).apply()
    }

    // Получение истории поиска
    fun getHistory(): List<Track> {
        val historyJson = sharedPreferences.getString(historyKey, null) ?: return emptyList()
        return try {
            gson.fromJson(historyJson, object : TypeToken<List<Track>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Очистка истории
    fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }

    // Сохранение состояния лайка для трека
    fun saveLike(trackId: Int, isLiked: Boolean) {
        val likesMap = getLikesMap().toMutableMap()
        if (isLiked) {
            likesMap[trackId] = true
        } else {
            likesMap.remove(trackId)
        }
        sharedPreferences.edit().putString(likesKey, gson.toJson(likesMap)).apply()
    }

    // Получение состояния лайка для трека
    fun isTrackLiked(trackId: Int): Boolean {
        return getLikesMap()[trackId] == true
    }

    // Получение всех лайков
    private fun getLikesMap(): Map<Int, Boolean> {
        val likesString = sharedPreferences.getString(likesKey, null) ?: return emptyMap()
        return try {
            gson.fromJson(likesString, object : TypeToken<Map<Int, Boolean>>() {}.type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}