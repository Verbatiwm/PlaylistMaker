package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface SearchTracksInteractor {
    fun search(query: String, consumer: (Result<List<Track>>) -> Unit): RequestHandle
}
