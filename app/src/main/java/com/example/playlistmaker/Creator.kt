package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.SearchTracksInteractor
import com.example.playlistmaker.domain.api.SettingsInteractor
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.SearchTracksInteractorImpl
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl

object Creator {
    private val tracksRepository by lazy { TracksRepositoryImpl(RetrofitNetworkClient().api) }

    fun provideSearchTracksInteractor(): SearchTracksInteractor =
        SearchTracksInteractorImpl(tracksRepository)

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor =
        SearchHistoryInteractorImpl(SearchHistoryRepositoryImpl(
            context.applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        ))

    fun provideSettingsInteractor(context: Context): SettingsInteractor =
        SettingsInteractorImpl(SettingsRepositoryImpl(
            context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        ))
}
