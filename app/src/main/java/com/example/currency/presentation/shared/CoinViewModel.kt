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
    val updatedAt: Long? = null,
    val isRefreshing: Boolean = false
)

data class FiatsUiState(
    val isLoading: Boolean = false,
    val currencies: List<CurrencyItem> = emptyList(),
    val selectedCurrency: CurrencyItem? = null,
    val updatedAt: Long? = null,
    val isRefreshing: Boolean = false
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

    fun loadCoins(forceRefresh: Boolean = false) {
        cryptoJob?.cancel()
        val requestId = ++cryptoRequestId
        cryptoJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getCryptoList(forceRefresh).collect { result ->
                if (requestId != cryptoRequestId) return@collect
                val selectedId = _uiState.value.selectedCoin?.currency?.id
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currencies = result.items.map { it.currency },
                    marketCoins = result.items,
                    selectedCoin = result.items.firstOrNull { it.currency.id == selectedId }
                        ?: _uiState.value.selectedCoin,
                    updatedAt = result.updatedAt,
                    isRefreshing = result.isRefreshing
                )
            }
        }
    }

    fun loadFiats(forceRefresh: Boolean = false) {
        fiatJob?.cancel()
        val requestId = ++fiatRequestId
        fiatJob = viewModelScope.launch {
            _fiatUiState.value = _fiatUiState.value.copy(isLoading = true)
            getFiatList(forceRefresh).collect { result ->
                if (requestId != fiatRequestId) return@collect
                _fiatUiState.value = _fiatUiState.value.copy(
                    isLoading = false,
                    currencies = result.items,
                    updatedAt = result.updatedAt,
                    isRefreshing = result.isRefreshing
                )
            }
        }
    }

    fun loadChart(id: String, days: Int = 1) {
        chartJob?.cancel() // Hủy ngay request cũ nếu đang chạy dở
        val requestId = ++chartRequestId
        chartJob = viewModelScope.launch { // Gán coroutine mới vào chartjob để tránh chạy quá nhiều request
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
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        _fiatUiState.value = _fiatUiState.value.copy(isRefreshing = true)
        loadCoins(forceRefresh = true)
        loadFiats(forceRefresh = true)
    }

    fun refreshCoins() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadCoins(forceRefresh = true)
    }

    fun selectMarketCoin(coin: CoinMarketItem) {
        _uiState.value = _uiState.value.copy(selectedCoin = coin)
    }

}
