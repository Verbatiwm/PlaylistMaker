package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.network.dto.SearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("search")
    fun search(@Query("term") text: String, @Query("entity") entity: String = "song"): Call<SearchResponseDto>

    @GET("lookup")
    fun lookup(@Query("id") trackId: Long): Call<SearchResponseDto>
}
