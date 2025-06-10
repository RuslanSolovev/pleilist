package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.HistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepositoryImpl(
    private val prefs: SharedPreferences,
    private val gson: Gson
) : HistoryRepository {

    private val KEY = "search_history"

    override fun getHistory(): List<Track> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun saveHistory(tracks: List<Track>) {
        val json = gson.toJson(tracks)
        prefs.edit().putString(KEY, json).apply()
    }

    override fun clearHistory() {
        prefs.edit().remove(KEY).apply()
    }
}