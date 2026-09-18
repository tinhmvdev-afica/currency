package com.example.currency.domain.repository

import com.example.currency.domain.model.ThemeMode
import com.example.currency.domain.model.ConversionPairPreference

interface UserPreferencesRepository {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
    fun getDefaultCurrency(): String
    fun setDefaultCurrency(code: String)
    fun getQuickCurrencies(): List<String>
    fun setQuickCurrencies(codes: List<String>)
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun getLanguageTag(): String?
    fun setLanguageTag(tag: String)
    fun getLastConversionPair(): ConversionPairPreference?
    fun setLastConversionPair(pair: ConversionPairPreference)
}
