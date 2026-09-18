package com.example.currency.data.remote.api

import com.example.currency.data.remote.dto.CoinMarketDto
import com.example.currency.data.remote.dto.MarketChartDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("coins/markets?vs_currency=usd")
    suspend fun getTopCoins(
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 5,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinMarketDto>

    @GET("coins/markets?vs_currency=usd")
    suspend fun getCoinMarkets(
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("ids") ids: String? = null,
        @Query("price_change_percentage") priceChange: String = "24h"
    ): List<CoinMarketDto>

    @GET("coins/{id}/market_chart?vs_currency=usd")
    suspend fun getCoinMarketChart(
        @Path("id") id: String,
        @Query("days") days: Int = 1
    ): MarketChartDto
}
