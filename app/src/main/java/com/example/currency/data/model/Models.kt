package com.example.currency.data.model

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale


data class CoinMarket(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("market_cap") val marketCap: Double? = null,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null
)

data class CurrencyFreaksDetail(
    @SerializedName("currencyCode") val id: String?,
    @SerializedName("currencyName") val name: String?,
    @SerializedName("countryCode") val countryCode: String?,
    val icon: String? = null
)


data class CurrencyFreaksResponse(
    val supportedCurrenciesMap: Map<String, CurrencyFreaksDetail>
)


/**
 * Model phản hồi từ endpoint /exchange_rates của CoinGecko
 */
data class ExchangeRatesResponse(
    @SerializedName("rates") val rates: Map<String, ExchangeRateItem>
)

data class ExchangeRateItem(
    @SerializedName("name") val name: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("value") val value: Double,
    @SerializedName("type") val type: String // "fiat" hoặc "crypto"
)

data class CurrencyFreaksRatesResponse(
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, String>
)

/**
 * Model thống nhất cho cả Crypto và Fiat dùng trên giao diện người dùng (UI)
 */
data class CurrencyItem(
    val id: String,
    val symbol: String,
    val name: String,
    val isCrypto: Boolean,
    val iconUrl: String? = null,
    val symbolChar: String = "",
    val priceInUsd: Double = 0.0,
    val priceChange24h: Double? = null,
    val marketCap: Double? = null,
    val marketCapRank: Int? = null
)

data class MarketChartResponse(
    @SerializedName("prices") val prices: List<List<Double>>
)
data class PricePoint(
    val timestamp: Long,
    val price: Double
)
