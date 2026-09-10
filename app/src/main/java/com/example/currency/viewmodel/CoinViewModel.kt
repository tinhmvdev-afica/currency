package com.example.currency.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.currency.data.model.CoinMarket
import com.example.currency.data.model.CurrencyFreaksDetail
import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class CryptoUiState(
    val isLoading: Boolean = false,
    val currencies: List<CurrencyItem> = emptyList(),
    val selectedCurrency: CurrencyItem? = null,
    val errorMessage: String? = null
)
data class FiatsUiState(
    val isLoading: Boolean = false,
    val currencies: List<CurrencyItem> = emptyList(),
    val selectedCurrency: CurrencyItem? = null,
    val errorMessage: String? = null
)
@HiltViewModel
class CoinViewModel  @Inject constructor (
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    private val _fiatUiState = MutableStateFlow(FiatsUiState())
    val fiatUiState: StateFlow<FiatsUiState> = _fiatUiState.asStateFlow()
    /**
     * Lấy danh sách coin theo loại tiền.
     *
     * Ví dụ:
     * currency = "vnd"
     * currency = "usd"
     * currency = "eur"
     */
    fun loadCoins(currency: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            repository.getCoinMarket(currency)
                .onSuccess { currencies ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currencies = currencies,
                        errorMessage = null
                    )
                }
                .onFailure { error ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra"
                    )
                }
        }
    }

    fun loadFiats() {

        viewModelScope.launch {

            _fiatUiState.value = _fiatUiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.getFiats()
                .onSuccess { currencies ->

                    _fiatUiState.value = _fiatUiState.value.copy(
                        isLoading = false,
                        currencies = currencies,
                        errorMessage = null
                    )
                }
                .onFailure { error ->

                    _fiatUiState.value = _fiatUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra"
                    )
                }
        }
    }
    fun refreshAll() {
        viewModelScope.launch {
            val cryptoResult = repository.getCoinMarket(currency = "usd", forceRefresh = true)
            val fiatResult = repository.getFiats(forceRefresh = true)
            if (cryptoResult.isSuccess && fiatResult.isSuccess
            ) {
                Log.d("REFRESH", "Refresh thành công")
            }
        }
    }
    /**
     * Lấy một coin cụ thể.
     *
     * coinId phải là CoinGecko ID:
     * bitcoin
     * ethereum
     * solana
     */


    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}

/** Supplies the repository dependency when Compose creates [CoinViewModel]. */
