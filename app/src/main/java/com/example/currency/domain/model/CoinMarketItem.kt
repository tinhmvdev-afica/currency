package com.example.currency.domain.model

data class CoinMarketItem(
    val currency: CurrencyItem,
    val currentPrice: Double,
    val priceChange24h: Double? = null,
    val low24h: Double? = null,
    val high24h: Double? = null,
    val marketCap: Double? = null,
    val marketCapRank: Int? = null
)
