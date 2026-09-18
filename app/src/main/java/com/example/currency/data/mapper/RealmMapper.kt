package com.example.currency.data.mapper

import com.example.currency.data.local.realm.CurrencyRealm
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.CurrencyItem

fun CurrencyRealm.toCurrencyItem(): CurrencyItem = CurrencyItem(
    id = id,
    symbol = symbol,
    name = name,
    isCrypto = isCrypto,
    iconUrl = iconUrl,
    symbolChar = symbolChar,
    priceInUsd = priceInUsd
)

fun CurrencyItem.toRealm(): CurrencyRealm = CurrencyRealm().also {
    it.id = id
    it.symbol = symbol
    it.name = name
    it.isCrypto = isCrypto
    it.iconUrl = iconUrl
    it.symbolChar = symbolChar
    it.priceInUsd = priceInUsd
}

fun CurrencyRealm.toCoinMarketItem(): CoinMarketItem = CoinMarketItem(
    currency = toCurrencyItem(),
    currentPrice = priceInUsd,
    priceChange24h = priceChange24h,
    low24h = low24h,
    high24h = high24h,
    marketCap = marketCap,
    marketCapRank = marketCapRank
)

fun CoinMarketItem.toRealm(): CurrencyRealm = currency.toRealm().also {
    it.isCrypto = true
    it.priceChange24h = priceChange24h
    it.low24h = low24h
    it.high24h = high24h
    it.marketCap = marketCap
    it.marketCapRank = marketCapRank
}
