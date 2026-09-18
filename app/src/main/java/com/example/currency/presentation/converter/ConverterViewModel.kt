package com.example.currency.presentation.converter

import androidx.lifecycle.ViewModel
import com.example.currency.domain.model.ConversionPairPreference
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.domain.model.PreferenceUpdate
import com.example.currency.domain.model.SavedCurrencySelection
import com.example.currency.domain.usecase.converter.CalculateConversionUseCase
import com.example.currency.domain.usecase.preferences.GetUserPreferencesUseCase
import com.example.currency.domain.usecase.preferences.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class QuickConversion(
    val currency: CurrencyItem,
    val amount: Double,
    val rate: Double
)

data class ConverterUiState(
    val fromCurrency: CurrencyItem? = null,
    val toCurrency: CurrencyItem? = null,
    val cryptoCurrencies: List<CurrencyItem> = emptyList(),
    val fiatCurrencies: List<CurrencyItem> = emptyList(),
    val inputAmount: Double = 1.0,
    val rate: Double = 0.0,
    val convertedAmount: Double = 0.0,
    val quickConversions: List<QuickConversion> = emptyList()
)

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val calculateConversion: CalculateConversionUseCase,
    private val getPreferences: GetUserPreferencesUseCase,
    private val updatePreferences: UpdateUserPreferencesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()
    private val savedPair = getPreferences().lastConversionPair
    private var lastPersistedPair = savedPair

    fun updateCryptoCurrencies(currencies: List<CurrencyItem>) {
        val current = _uiState.value
        val from = resolveCurrency(current.fromCurrency, savedPair?.from, currencies, current.fiatCurrencies, true)
        val to = resolveCurrency(current.toCurrency, savedPair?.to, currencies, current.fiatCurrencies, false)
        recalculate(current.copy(cryptoCurrencies = currencies, fromCurrency = from, toCurrency = to))
    }

    fun updateFiatCurrencies(currencies: List<CurrencyItem>) {
        val current = _uiState.value
        val from = resolveCurrency(current.fromCurrency, savedPair?.from, current.cryptoCurrencies, currencies, true)
        val to = resolveCurrency(current.toCurrency, savedPair?.to, current.cryptoCurrencies, currencies, false)
        recalculate(current.copy(fiatCurrencies = currencies, fromCurrency = from, toCurrency = to))
    }

    fun updateAmount(amount: Double) = recalculate(_uiState.value.copy(inputAmount = amount))

    fun selectFrom(currency: CurrencyItem) = recalculate(_uiState.value.copy(fromCurrency = currency))

    fun selectTo(currency: CurrencyItem) = recalculate(_uiState.value.copy(toCurrency = currency))

    fun selectQuickCurrency(symbol: String) {
        val current = _uiState.value
        val selected = (current.cryptoCurrencies + current.fiatCurrencies).firstOrNull {
            it.symbol.equals(symbol, ignoreCase = true)
        } ?: return
        selectTo(selected)
    }

    fun swap() {
        val current = _uiState.value
        if (current.fromCurrency == null || current.toCurrency == null) return
        recalculate(current.copy(fromCurrency = current.toCurrency, toCurrency = current.fromCurrency))
    }

    fun refreshQuickCurrencies() = recalculate(_uiState.value)

    private fun recalculate(state: ConverterUiState) {
        val from = state.fromCurrency
        val to = state.toCurrency
        val allCurrencies = state.cryptoCurrencies + state.fiatCurrencies
        val quickConversions = if (from == null) emptyList() else {
            getPreferences().quickCurrencies.mapNotNull { code ->
                val currency = allCurrencies.firstOrNull {
                    it.symbol.equals(code, ignoreCase = true)
                } ?: return@mapNotNull null
                QuickConversion(
                    currency = currency,
                    amount = if (currency.priceInUsd > 0.0) {
                        calculateConversion(state.inputAmount, from, currency)
                    } else 0.0,
                    rate = calculateConversion(1.0, from, currency)
                )
            }
        }
        _uiState.value = state.copy(
            rate = if (from != null && to != null) calculateConversion(1.0, from, to) else 0.0,
            convertedAmount = if (from != null && to != null) {
                calculateConversion(state.inputAmount, from, to)
            } else 0.0,
            quickConversions = quickConversions
        )
        if (from != null && to != null) {
            val pair = ConversionPairPreference(
                from = SavedCurrencySelection(from.id, from.isCrypto),
                to = SavedCurrencySelection(to.id, to.isCrypto)
            )
            if (pair != lastPersistedPair) {
                updatePreferences(PreferenceUpdate.LastConversionPair(pair))
                lastPersistedPair = pair
            }
        }
    }

    private fun resolveCurrency(
        current: CurrencyItem?,
        saved: SavedCurrencySelection?,
        cryptoCurrencies: List<CurrencyItem>,
        fiatCurrencies: List<CurrencyItem>,
        isFrom: Boolean
    ): CurrencyItem? {
        if (current != null) {
            val refreshedCurrencies = if (current.isCrypto) cryptoCurrencies else fiatCurrencies
            return refreshedCurrencies.firstOrNull { it.id == current.id } ?: current
        }

        if (saved != null) {
            val savedCurrencies = if (saved.isCrypto) cryptoCurrencies else fiatCurrencies
            if (savedCurrencies.isEmpty()) return null
            return savedCurrencies.firstOrNull { it.id == saved.id } ?: savedCurrencies.firstOrNull()
        }

        val defaultCurrencies = if (isFrom) cryptoCurrencies else fiatCurrencies
        return defaultCurrencies.firstOrNull()
    }
}
