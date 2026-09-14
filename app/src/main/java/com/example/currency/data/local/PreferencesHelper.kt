package com.example.currency.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object PreferencesHelper {
    private const val PREFS_NAME = "coinflux_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
    private const val KEY_DEFAULT_CURRENCY = "default_currency"
    private const val KEY_QUICK_CURRENCIES = "quick_currencies"
    private const val KEY_THEME_MODE = "theme_mode"

    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    val DEFAULT_QUICK_CURRENCIES = listOf("USD", "EUR", "ETH", "SOL", "VND")
    const val MAX_QUICK_CURRENCIES = 5

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun getDefaultCurrency(context: Context): String {
        return getPrefs(context).getString(KEY_DEFAULT_CURRENCY, "VND") ?: "VND"
    }

    fun setDefaultCurrency(context: Context, currencyCode: String) {
        getPrefs(context).edit().putString(KEY_DEFAULT_CURRENCY, currencyCode).apply()
    }

    fun getQuickCurrencies(context: Context): List<String> {
        val raw = getPrefs(context).getString(KEY_QUICK_CURRENCIES, null)
        return if (!raw.isNullOrBlank()) {
            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(MAX_QUICK_CURRENCIES)
        } else {
            DEFAULT_QUICK_CURRENCIES
        }
    }

    fun setQuickCurrencies(context: Context, currencies: List<String>) {
        val toSave = currencies.take(MAX_QUICK_CURRENCIES).joinToString(",")
        getPrefs(context).edit().putString(KEY_QUICK_CURRENCIES, toSave).apply()
    }

    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, THEME_DARK) ?: THEME_DARK
    }

    fun setThemeMode(context: Context, themeMode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, themeMode).apply()
    }

    fun applyTheme(themeMode: String) {
        val nightMode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
