package com.example.currency.data.local

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
    private const val PREFS_NAME = "coinflux_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
    private const val KEY_DEFAULT_CURRENCY = "default_currency"

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
}
