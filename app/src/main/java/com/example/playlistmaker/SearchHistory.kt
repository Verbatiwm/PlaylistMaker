package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val prefs: SharedPreferences) {



    private val gson = Gson()

    fun getHistory(): List<Track> {
        val json = prefs.getString(KEY, null) ?: return emptyList()

        val type = object : TypeToken<MutableList<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveHistory(list: List<Track>) {
        val json = gson.toJson(list)
        prefs.edit()
            .putString(KEY, json)
            .apply()
    }

    fun clearHistory() {
        prefs.edit()
            .remove(KEY)
            .apply()
    }


    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > MAX_SIZE) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    companion object {
        private const val KEY = "search_history"
        private const val MAX_SIZE = 10
    }
}