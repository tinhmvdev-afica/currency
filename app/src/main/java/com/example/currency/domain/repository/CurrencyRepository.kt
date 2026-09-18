package com.example.currency.domain.repository

import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.ChartLoadResult
import com.example.currency.domain.model.CurrencyItem
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    suspend fun getCoinMarket(forceRefresh: Boolean = false): Result<List<CoinMarketItem>>
    suspend fun getFiats(forceRefresh: Boolean = false): Result<List<CurrencyItem>>
    fun getMarketChart(id: String, days: Int = 1): Flow<ChartLoadResult>
}
