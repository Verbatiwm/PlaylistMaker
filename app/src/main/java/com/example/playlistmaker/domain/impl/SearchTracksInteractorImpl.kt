package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.RequestHandle
import com.example.playlistmaker.domain.api.SearchTracksInteractor
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.Track

class SearchTracksInteractorImpl(
    private val repository: TracksRepository
) : SearchTracksInteractor {
    override fun search(query: String, consumer: (Result<List<Track>>) -> Unit): RequestHandle =
        repository.search(query.trim(), consumer)

}
