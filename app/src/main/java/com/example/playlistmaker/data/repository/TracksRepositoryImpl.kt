package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.network.dto.SearchResponseDto
import com.example.playlistmaker.data.network.dto.TrackDto
import com.example.playlistmaker.domain.api.RequestHandle
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(private val api: ITunesApi) : TracksRepository {
    override fun search(query: String, consumer: (Result<List<Track>>) -> Unit): RequestHandle {
        val call = api.search(query)
        call.enqueue(callback({ response -> response.results.map(::map) }, consumer))
        return RequestHandle { call.cancel() }
    }

    private fun <T> callback(
        mapper: (SearchResponseDto) -> T,
        consumer: (Result<T>) -> Unit
    ) = object : Callback<SearchResponseDto> {
        override fun onResponse(call: Call<SearchResponseDto>, response: Response<SearchResponseDto>) {
            if (call.isCanceled) return
            val body = response.body()
            if (response.isSuccessful && body != null) consumer(Result.success(mapper(body)))
            else consumer(Result.failure(IllegalStateException("HTTP ${response.code()}")))
        }

        override fun onFailure(call: Call<SearchResponseDto>, error: Throwable) {
            if (!call.isCanceled) consumer(Result.failure(error))
        }
    }

    private fun map(dto: TrackDto) = Track(
        dto.trackId ?: 0, dto.trackName ?: "Unknown", dto.artistName ?: "Unknown",
        dto.trackTimeMillis ?: 0, dto.artworkUrl100.orEmpty(), dto.collectionName,
        dto.releaseDate, dto.primaryGenreName ?: "Unknown", dto.country ?: "Unknown", dto.previewUrl
    )
}
