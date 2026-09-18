package com.example.currency.domain.model

data class CurrencyItem(
    val id: String,
    val symbol: String,
    val name: String,
    val isCrypto: Boolean,
    val iconUrl: String? = null,
    val symbolChar: String = "",
    val priceInUsd: Double = 0.0
)
