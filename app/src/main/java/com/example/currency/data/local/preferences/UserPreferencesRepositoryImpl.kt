package com.example.currency.data.local.preferences

import android.content.Context
import com.example.currency.domain.model.ConversionPairPreference
import com.example.currency.domain.model.QuickCurrencyPolicy
import com.example.currency.domain.model.SavedCurrencySelection
import com.example.currency.domain.model.ThemeMode
import com.example.currency.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : UserPreferencesRepository {
    private val prefs = context.getSharedPreferences("coinflux_prefs", Context.MODE_PRIVATE)

    override fun isOnboardingCompleted(): Boolean = prefs.getBoolean("is_onboarding_completed", false)

    override fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("is_onboarding_completed", completed).apply()
    }

    override fun getDefaultCurrency(): String = prefs.getString("default_currency", "VND") ?: "VND"

    override fun setDefaultCurrency(code: String) {
        prefs.edit().putString("default_currency", code).apply()
    }

    override fun getQuickCurrencies(): List<String> {
        val raw = prefs.getString("quick_currencies", null)
        return if (raw.isNullOrBlank()) QuickCurrencyPolicy.DEFAULT_CURRENCIES else
            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                .take(QuickCurrencyPolicy.MAX_CURRENCIES)
    }

    override fun setQuickCurrencies(codes: List<String>) {
        prefs.edit().putString(
            "quick_currencies",
            codes.take(QuickCurrencyPolicy.MAX_CURRENCIES).joinToString(",")
        ).apply()
    }

    override fun getThemeMode(): ThemeMode = when (prefs.getString("theme_mode", "dark")) {
        "light" -> ThemeMode.LIGHT
        "system" -> ThemeMode.SYSTEM
        else -> ThemeMode.DARK
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name.lowercase()).apply()
    }

    override fun getLanguageTag(): String? = prefs.getString("app_language", null)

    override fun setLanguageTag(tag: String) {
        prefs.edit().putString("app_language", tag).apply()
    }

    override fun getLastConversionPair(): ConversionPairPreference? {
        val fromId = prefs.getString("last_conversion_from_id", null) ?: return null
        val toId = prefs.getString("last_conversion_to_id", null) ?: return null
        return ConversionPairPreference(
            from = SavedCurrencySelection(
                id = fromId,
                isCrypto = prefs.getBoolean("last_conversion_from_is_crypto", false)
            ),
            to = SavedCurrencySelection(
                id = toId,
                isCrypto = prefs.getBoolean("last_conversion_to_is_crypto", false)
            )
        )
    }

    override fun setLastConversionPair(pair: ConversionPairPreference) {
        prefs.edit()
            .putString("last_conversion_from_id", pair.from.id)
            .putBoolean("last_conversion_from_is_crypto", pair.from.isCrypto)
            .putString("last_conversion_to_id", pair.to.id)
            .putBoolean("last_conversion_to_is_crypto", pair.to.isCrypto)
            .apply()
    }
}
