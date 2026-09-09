package com.example.currency.data.api

import com.example.currency.data.model.CoinMarket
import com.example.currency.data.model.ExchangeRatesResponse
import retrofit2.http.GET
import retrofit2.http.Header
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
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("ids") ids: String? = null,
        @Query("price_change_percentage") priceChange: String = "24h",
        @Header("x-cg-demo-api-key") demoApiKey: String? = "CG-jvm7rDh1hweD1s99Wk3yhicn"

    ): List<CoinMarket>

    /**
     * Lấy bảng tỷ giá quy đổi của tất cả các đồng Fiat & Crypto theo chuẩn Bitcoin (BTC)
     * Rất thích hợp để tính tỷ giá chéo giữa Fiat ⇄ Fiat, Crypto ⇄ Fiat và Crypto ⇄ Crypto
     */
    @GET("exchange_rates")
    suspend fun getExchangeRates(
        @Header("x-cg-demo-api-key") demoApiKey: String? = null
    ): ExchangeRatesResponse
}
