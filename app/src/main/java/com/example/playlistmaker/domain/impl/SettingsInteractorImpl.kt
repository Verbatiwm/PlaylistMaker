package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.SettingsInteractor
import com.example.playlistmaker.domain.api.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {
    override fun isDarkThemeEnabled(): Boolean = repository.isDarkThemeEnabled()
    override fun setDarkTheme(enabled: Boolean) = repository.setDarkTheme(enabled)
}
