package com.example.currency.data.mapper

import com.example.currency.data.remote.dto.CoinMarketDto
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.CurrencyItem

fun CoinMarketDto.toCoinMarketItem(): CoinMarketItem = CoinMarketItem(
    currency = CurrencyItem(
        id = id,
        symbol = symbol.uppercase(),
        name = name,
        isCrypto = true,
        iconUrl = image,
        symbolChar = symbol.take(3).uppercase(),
        priceInUsd = currentPrice
    ),
    currentPrice = currentPrice,
    priceChange24h = priceChangePercentage24h,
    low24h = low24h,
    high24h = high24h,
    marketCap = marketCap,
    marketCapRank = marketCapRank
)
