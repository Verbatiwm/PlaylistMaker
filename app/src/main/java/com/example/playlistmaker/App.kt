package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    private lateinit var prefs: SharedPreferences

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        darkTheme = prefs.getBoolean("dark_theme", false)

        switchTheme(darkTheme)
    }

    fun switchTheme(enabled: Boolean) {
        darkTheme = enabled


        prefs.edit().putBoolean("dark_theme", enabled).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (enabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}