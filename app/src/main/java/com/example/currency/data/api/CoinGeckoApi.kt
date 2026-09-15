package com.example.currency.data.api

import com.example.currency.data.model.CoinMarket
import com.example.currency.data.model.ExchangeRatesResponse
import com.example.currency.data.model.MarketChartResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {

    /**
     * Lấy danh sách Top Crypto theo vốn hóa kèm giá, logo, thay đổi 24h
     */
    @GET("coins/markets")
    suspend fun getTopCoins(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 5,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Header("x-cg-demo-api-key") demoApiKey: String? = "CG-jvm7rDh1hweD1s99Wk3yhicn"
    ): List<CoinMarket>

    @GET("coins/markets")
    suspend fun getCoinMarkets(
        @Query("vs_currency") currency: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("ids") ids: String? = null,
        @Query("price_change_percentage") priceChange: String = "24h",
        @Header("x-cg-demo-api-key") demoApiKey: String? = "CG-jvm7rDh1hweD1s99Wk3yhicn"

    ): List<CoinMarket>
    @GET("coins/{id}/market_chart")
    suspend fun getCoinMarketChart(
        @Path("id") id :String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: Int = 1,
        @Header("x-cg-demo-api-key") demoApiKey: String? = "CG-jvm7rDh1hweD1s99Wk3yhicn"

    ): MarketChartResponse

}
