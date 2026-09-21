package com.example.currency.domain.repository

import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.ChartLoadResult
import com.example.currency.domain.model.CurrencyListLoadResult
import com.example.currency.domain.model.CurrencyItem
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getCoinMarket(forceRefresh: Boolean = false): Flow<CurrencyListLoadResult<CoinMarketItem>>
    fun getFiats(forceRefresh: Boolean = false): Flow<CurrencyListLoadResult<CurrencyItem>>
    suspend fun refreshCoinMarket(): Result<List<CoinMarketItem>>
    suspend fun refreshFiats(): Result<List<CurrencyItem>>
    fun getMarketChart(id: String, days: Int = 1): Flow<ChartLoadResult>
}
