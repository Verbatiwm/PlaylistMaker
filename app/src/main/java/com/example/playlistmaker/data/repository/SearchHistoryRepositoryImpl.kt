package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.data.storage.dto.TrackStorageDto
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val prefs: SharedPreferences,
    private val gson: Gson
) : SearchHistoryRepository {

    override fun getHistory(): List<Track> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<TrackStorageDto>>() {}.type
        return runCatching { gson.fromJson<List<TrackStorageDto>>(json, type).map(::toDomain) }
            .getOrDefault(emptyList())
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        prefs.edit().putString(KEY, gson.toJson(history.take(MAX_SIZE).map(::toDto))).apply()
    }

    override fun clearHistory() { prefs.edit().remove(KEY).apply() }

    private fun toDto(t: Track) = TrackStorageDto(t.trackId, t.trackName, t.artistName, t.trackTimeMillis,
        t.artworkUrl100, t.collectionName, t.releaseDate, t.primaryGenreName, t.country, t.previewUrl)
    private fun toDomain(t: TrackStorageDto) = Track(t.trackId, t.trackName, t.artistName, t.trackTimeMillis,
        t.artworkUrl100, t.collectionName, t.releaseDate, t.primaryGenreName, t.country, t.previewUrl)

    private companion object { const val KEY = "search_history"; const val MAX_SIZE = 10 }
}
