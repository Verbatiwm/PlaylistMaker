package com.example.playlistmaker.data.network

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient(gson: Gson) {
    val api: ITunesApi = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ITunesApi::class.java)
}
