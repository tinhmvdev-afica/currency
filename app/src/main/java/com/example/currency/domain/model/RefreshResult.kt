package com.example.currency.domain.model

data class RefreshResult(
    val crypto: Result<List<CoinMarketItem>>,
    val fiat: Result<List<CurrencyItem>>
)
