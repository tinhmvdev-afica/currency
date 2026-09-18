package com.example.currency.presentation.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currency.data.network.NetworkMonitor
import com.example.currency.domain.model.ChartLoadResult
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.domain.model.PricePoint
import com.example.currency.domain.usecase.currency.GetCryptoListUseCase
import com.example.currency.domain.usecase.currency.GetFiatListUseCase
import com.example.currency.domain.usecase.currency.RefreshCurrenciesUseCase
import com.example.currency.domain.usecase.market.GetMarketChartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CryptoUiState(
    val isLoading: Boolean = false,
    val currencies: List<CurrencyItem> = emptyList(),
    val marketCoins: List<CoinMarketItem> = emptyList(),
    val selectedCoin: CoinMarketItem? = null,
    val errorMessage: String? = null
)

data class FiatsUiState(
    val isLoading: Boolean = false,
    val currencies: List<CurrencyItem> = emptyList(),
    val selectedCurrency: CurrencyItem? = null,
    val errorMessage: String? = null
)

data class MarketChartUiState(
    val isLoading: Boolean = false,
    val chartData: List<PricePoint> = emptyList(),
    val isCached: Boolean = false,
    val updatedAt: Long? = null,
    val isOffline: Boolean = false,
    val hasNoOfflineChart: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshFailed: Boolean = false,
    val hasLoadError: Boolean = false
)

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val getCryptoList: GetCryptoListUseCase,
    private val getFiatList: GetFiatListUseCase,
    private val refreshCurrencies: RefreshCurrenciesUseCase,
    private val getMarketChart: GetMarketChartUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    private val _fiatUiState = MutableStateFlow(FiatsUiState())
    val fiatUiState: StateFlow<FiatsUiState> = _fiatUiState.asStateFlow()

    private val _chartUiState = MutableStateFlow(MarketChartUiState())
    val chartUiState: StateFlow<MarketChartUiState> = _chartUiState.asStateFlow()

    private var cryptoJob: Job? = null
    private var fiatJob: Job? = null
    private var chartJob: Job? = null
    private var refreshJob: Job? = null
    private var cryptoRequestId = 0L
    private var fiatRequestId = 0L
    private var chartRequestId = 0L

    init {
        loadCoins()
        loadFiats()
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _chartUiState.value = _chartUiState.value.copy(isOffline = !isOnline)
            }
        }
    }

    fun loadCoins() {
        cryptoJob?.cancel()
        val requestId = ++cryptoRequestId
        cryptoJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getCryptoList()
                .onSuccess { marketCoins ->
                    if (requestId != cryptoRequestId) return@onSuccess
                    val selectedId = _uiState.value.selectedCoin?.currency?.id
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currencies = marketCoins.map { it.currency },
                        marketCoins = marketCoins,
                        selectedCoin = marketCoins.firstOrNull { it.currency.id == selectedId }
                            ?: _uiState.value.selectedCoin,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    if (requestId != cryptoRequestId) return@onFailure
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra"
                    )
                }
        }
    }

    fun loadFiats() {
        fiatJob?.cancel()
        val requestId = ++fiatRequestId
        fiatJob = viewModelScope.launch {
            _fiatUiState.value = _fiatUiState.value.copy(isLoading = true, errorMessage = null)
            getFiatList()
                .onSuccess { currencies ->
                    if (requestId != fiatRequestId) return@onSuccess
                    _fiatUiState.value = _fiatUiState.value.copy(
                        isLoading = false,
                        currencies = currencies,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    if (requestId != fiatRequestId) return@onFailure
                    _fiatUiState.value = _fiatUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra"
                    )
                }
        }
    }

    fun loadChart(id: String, days: Int = 1) {
        chartJob?.cancel()
        val requestId = ++chartRequestId
        chartJob = viewModelScope.launch {
            _chartUiState.value = MarketChartUiState(
                isLoading = true,
                isOffline = !networkMonitor.hasInternetConnection()
            )
            getMarketChart(id, days).collect { result ->
                if (requestId != chartRequestId) return@collect
                _chartUiState.value = when (result) {
                    is ChartLoadResult.Data -> MarketChartUiState(
                        isLoading = false,
                        chartData = result.points,
                        isCached = result.isCached,
                        updatedAt = result.updatedAt,
                        isOffline = result.isOffline,
                        isRefreshing = result.isRefreshing,
                        refreshFailed = result.refreshFailed
                    )

                    ChartLoadResult.OfflineNoCache -> MarketChartUiState(
                        isLoading = false,
                        isOffline = true,
                        hasNoOfflineChart = true
                    )

                    ChartLoadResult.NetworkError -> MarketChartUiState(
                        isLoading = false,
                        hasLoadError = true
                    )
                }
            }
        }
    }

    fun refreshAll() {
        cryptoJob?.cancel()
        fiatJob?.cancel()
        refreshJob?.cancel()
        val cryptoId = ++cryptoRequestId
        val fiatId = ++fiatRequestId
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        _fiatUiState.value = _fiatUiState.value.copy(isLoading = true, errorMessage = null)
        refreshJob = viewModelScope.launch {
            val result = refreshCurrencies()
            if (cryptoId == cryptoRequestId) {
                result.crypto.onSuccess { coins ->
                    val selectedId = _uiState.value.selectedCoin?.currency?.id
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currencies = coins.map { it.currency },
                        marketCoins = coins,
                        selectedCoin = coins.firstOrNull { it.currency.id == selectedId }
                            ?: _uiState.value.selectedCoin,
                        errorMessage = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra"
                    )
                }
            }
            if (fiatId == fiatRequestId) {
                result.fiat.onSuccess { currencies ->
                    _fiatUiState.value = _fiatUiState.value.copy(
                        isLoading = false,
                        currencies = currencies,
                        errorMessage = null
                    )
                }.onFailure { error ->
                    _fiatUiState.value = _fiatUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Có lỗi xảy ra"
                    )
                }
            }
        }
    }

    fun selectMarketCoin(coin: CoinMarketItem) {
        _uiState.value = _uiState.value.copy(selectedCoin = coin)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
