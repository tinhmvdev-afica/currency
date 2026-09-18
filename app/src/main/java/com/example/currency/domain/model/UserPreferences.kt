package com.example.currency.domain.model

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val defaultCurrency: String,
    val quickCurrencies: List<String>,
    val themeMode: ThemeMode,
    val languageTag: String?,
    val lastConversionPair: ConversionPairPreference?
) {
    companion object {
        const val DEFAULT_LANGUAGE_TAG = "en"
    }
}

data class SavedCurrencySelection(
    val id: String,
    val isCrypto: Boolean
)

data class ConversionPairPreference(
    val from: SavedCurrencySelection,
    val to: SavedCurrencySelection
)

object QuickCurrencyPolicy {
    const val MAX_CURRENCIES = 5
    val DEFAULT_CURRENCIES = listOf("USD", "EUR", "ETH", "SOL", "VND")
}

sealed interface PreferenceUpdate {
    data class DefaultCurrency(val code: String) : PreferenceUpdate
    data class QuickCurrencies(val codes: List<String>) : PreferenceUpdate
    data class Theme(val mode: ThemeMode) : PreferenceUpdate
    data class Language(val tag: String) : PreferenceUpdate
    data class OnboardingCompleted(val completed: Boolean) : PreferenceUpdate
    data class LastConversionPair(val pair: ConversionPairPreference) : PreferenceUpdate
    data class CompleteOnboarding(val quickCurrencies: List<String>) : PreferenceUpdate
}
