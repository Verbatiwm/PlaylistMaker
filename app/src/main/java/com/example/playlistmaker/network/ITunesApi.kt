package com.example.playlistmaker.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {

    @GET("search")
    fun search(
        @Query("term") text: String,
        @Query("entity") entity: String = "song"
    ): Call<SearchResponse>

    @GET("lookup")
    fun lookup(
        @Query("id") trackId: Long
    ): Call<SearchResponse>
}
