package com.example.currency

import com.example.currency.domain.model.CurrencyItem
import com.example.currency.domain.model.ThemeMode
import com.example.currency.domain.model.ConversionPairPreference
import com.example.currency.domain.model.SavedCurrencySelection
import com.example.currency.domain.repository.UserPreferencesRepository
import com.example.currency.domain.usecase.converter.CalculateConversionUseCase
import com.example.currency.domain.usecase.preferences.GetUserPreferencesUseCase
import com.example.currency.domain.usecase.preferences.UpdateUserPreferencesUseCase
import com.example.currency.presentation.converter.ConverterViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterViewModelTest {
    @Test
    fun newPricesUpdateResultWithoutChangingSelectedPair() {
        val preferences = object : UserPreferencesRepository {
            override fun isOnboardingCompleted() = true
            override fun setOnboardingCompleted(completed: Boolean) = Unit
            override fun getDefaultCurrency() = "USD"
            override fun setDefaultCurrency(code: String) = Unit
            override fun getQuickCurrencies() = listOf("USD")
            override fun setQuickCurrencies(codes: List<String>) = Unit
            override fun getThemeMode() = ThemeMode.SYSTEM
            override fun setThemeMode(mode: ThemeMode) = Unit
            override fun getLanguageTag(): String? = null
            override fun setLanguageTag(tag: String) = Unit
            override fun getLastConversionPair(): ConversionPairPreference? = null
            override fun setLastConversionPair(pair: ConversionPairPreference) = Unit
        }
        val viewModel = ConverterViewModel(
            CalculateConversionUseCase(),
            GetUserPreferencesUseCase(preferences),
            UpdateUserPreferencesUseCase(preferences)
        )
        val bitcoin = CurrencyItem("bitcoin", "BTC", "Bitcoin", true, priceInUsd = 100.0)
        val ethereum = CurrencyItem("ethereum", "ETH", "Ethereum", true, priceInUsd = 50.0)
        val dollar = CurrencyItem("usd", "USD", "US Dollar", false, priceInUsd = 1.0)

        viewModel.updateCryptoCurrencies(listOf(bitcoin, ethereum))
        viewModel.updateFiatCurrencies(listOf(dollar))
        viewModel.selectFrom(ethereum)
        viewModel.updateAmount(2.0)
        assertEquals(100.0, viewModel.uiState.value.convertedAmount, 0.0)

        viewModel.updateCryptoCurrencies(listOf(bitcoin, ethereum.copy(priceInUsd = 75.0)))
        val state = viewModel.uiState.value
        assertEquals("ethereum", state.fromCurrency?.id)
        assertEquals("usd", state.toCurrency?.id)
        assertEquals(150.0, state.convertedAmount, 0.0)
        assertEquals(150.0, state.quickConversions.single().amount, 0.0)
    }

    @Test
    fun restoresLastConversionPairAfterCurrenciesLoad() {
        val savedPair = ConversionPairPreference(
            from = SavedCurrencySelection("ethereum", true),
            to = SavedCurrencySelection("VND", false)
        )
        var persistedPair: ConversionPairPreference? = null
        val preferences = object : UserPreferencesRepository {
            override fun isOnboardingCompleted() = true
            override fun setOnboardingCompleted(completed: Boolean) = Unit
            override fun getDefaultCurrency() = "USD"
            override fun setDefaultCurrency(code: String) = Unit
            override fun getQuickCurrencies() = emptyList<String>()
            override fun setQuickCurrencies(codes: List<String>) = Unit
            override fun getThemeMode() = ThemeMode.SYSTEM
            override fun setThemeMode(mode: ThemeMode) = Unit
            override fun getLanguageTag(): String? = null
            override fun setLanguageTag(tag: String) = Unit
            override fun getLastConversionPair() = savedPair
            override fun setLastConversionPair(pair: ConversionPairPreference) { persistedPair = pair }
        }
        val viewModel = ConverterViewModel(
            CalculateConversionUseCase(),
            GetUserPreferencesUseCase(preferences),
            UpdateUserPreferencesUseCase(preferences)
        )
        val bitcoin = CurrencyItem("bitcoin", "BTC", "Bitcoin", true, priceInUsd = 100.0)
        val ethereum = CurrencyItem("ethereum", "ETH", "Ethereum", true, priceInUsd = 50.0)
        val dong = CurrencyItem("VND", "VND", "Vietnamese Dong", false, priceInUsd = 0.00004)

        viewModel.updateCryptoCurrencies(listOf(bitcoin, ethereum))
        assertEquals("ethereum", viewModel.uiState.value.fromCurrency?.id)
        assertEquals(null, viewModel.uiState.value.toCurrency)

        viewModel.updateFiatCurrencies(listOf(dong))
        assertEquals("ethereum", viewModel.uiState.value.fromCurrency?.id)
        assertEquals("VND", viewModel.uiState.value.toCurrency?.id)

        viewModel.selectFrom(bitcoin)
        assertEquals("bitcoin", persistedPair?.from?.id)
        assertEquals("VND", persistedPair?.to?.id)
    }
}
