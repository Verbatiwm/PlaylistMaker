package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.domain.api.SettingsInteractor

class App : Application() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate() {
        super.onCreate()

        settingsInteractor = Creator.provideSettingsInteractor(this)
        settingsInteractor.setDarkTheme(settingsInteractor.isDarkThemeEnabled())
    }
}
