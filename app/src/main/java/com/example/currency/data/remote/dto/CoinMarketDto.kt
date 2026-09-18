package com.example.currency.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CoinMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("market_cap") val marketCap: Double? = null,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @SerializedName("low_24h") val low24h: Double? = null,
    @SerializedName("high_24h") val high24h: Double? = null
)
