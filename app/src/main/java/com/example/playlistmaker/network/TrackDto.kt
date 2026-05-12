package com.example.playlistmaker.network

data class TrackDto(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
)