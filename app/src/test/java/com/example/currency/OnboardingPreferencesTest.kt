package com.example.currency

import com.example.currency.domain.model.PreferenceUpdate
import com.example.currency.domain.model.ThemeMode
import com.example.currency.domain.model.ConversionPairPreference
import com.example.currency.domain.repository.UserPreferencesRepository
import com.example.currency.domain.usecase.preferences.UpdateUserPreferencesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingPreferencesTest {
    @Test
    fun completingOnboardingSavesAtMostFiveQuickCurrencies() {
        val preferences = FakePreferences()

        UpdateUserPreferencesUseCase(preferences)(
            PreferenceUpdate.CompleteOnboarding(listOf("USD", "EUR", "BTC", "ETH", "VND", "JPY"))
        )

        assertTrue(preferences.completed)
        assertEquals(listOf("USD", "EUR", "BTC", "ETH", "VND"), preferences.savedQuickCurrencies)
    }

    private class FakePreferences : UserPreferencesRepository {
        var completed = false
        var savedQuickCurrencies = emptyList<String>()

        override fun isOnboardingCompleted() = completed
        override fun setOnboardingCompleted(completed: Boolean) { this.completed = completed }
        override fun getDefaultCurrency() = "VND"
        override fun setDefaultCurrency(code: String) = Unit
        override fun getQuickCurrencies() = savedQuickCurrencies
        override fun setQuickCurrencies(codes: List<String>) { savedQuickCurrencies = codes }
        override fun getThemeMode() = ThemeMode.DARK
        override fun setThemeMode(mode: ThemeMode) = Unit
        override fun getLanguageTag(): String? = null
        override fun setLanguageTag(tag: String) = Unit
        override fun getLastConversionPair(): ConversionPairPreference? = null
        override fun setLastConversionPair(pair: ConversionPairPreference) = Unit
    }
}
