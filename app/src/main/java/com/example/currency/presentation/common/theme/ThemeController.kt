package com.example.currency.presentation.common.theme

import androidx.appcompat.app.AppCompatDelegate
import com.example.currency.domain.model.ThemeMode
import javax.inject.Inject

class ThemeController @Inject constructor() {
    fun apply(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
