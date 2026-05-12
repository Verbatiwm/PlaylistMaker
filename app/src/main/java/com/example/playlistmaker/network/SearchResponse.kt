package com.example.playlistmaker.network

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)