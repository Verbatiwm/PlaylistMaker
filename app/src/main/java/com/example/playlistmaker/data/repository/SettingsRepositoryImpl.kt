package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.api.SettingsRepository

class SettingsRepositoryImpl(private val prefs: SharedPreferences) : SettingsRepository {
    override fun isDarkThemeEnabled() = prefs.getBoolean(KEY_DARK_THEME, false)

    override fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private companion object { const val KEY_DARK_THEME = "dark_theme" }
}
