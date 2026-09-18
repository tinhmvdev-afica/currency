package com.example.currency

import com.example.currency.data.local.realm.CurrencyRealm
import com.example.currency.data.mapper.toCoinMarketItem
import com.example.currency.data.mapper.toCurrencyItem
import com.example.currency.data.mapper.toFiatCurrencyItems
import com.example.currency.data.mapper.toRealm
import com.example.currency.data.remote.dto.CoinMarketDto
import com.example.currency.data.remote.dto.CurrencyFreaksDetailDto
import com.example.currency.data.remote.dto.CurrencyFreaksRatesDto
import com.example.currency.data.remote.dto.CurrencyFreaksResponseDto
import com.example.currency.domain.model.CurrencyItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DataMapperTest {
    @Test
    fun coinMarketDtoMapsToCoinMarketItem() {
        val dto = CoinMarketDto(
            id = "bitcoin", symbol = "btc", name = "Bitcoin", image = "icon",
            currentPrice = 100.0, marketCap = 2_000.0, marketCapRank = 1
        )
        val market = dto.toCoinMarketItem()

        assertEquals("BTC", market.currency.symbol)
        assertEquals(100.0, market.currency.priceInUsd, 0.0)
        assertEquals(1, market.marketCapRank)
    }

    @Test
    fun currencyFreaksMapperFiltersCryptoAndConvertsRates() {
        val supported = CurrencyFreaksResponseDto(mapOf(
            "USD" to CurrencyFreaksDetailDto("USD", "US Dollar", "US"),
            "EUR" to CurrencyFreaksDetailDto("EUR", "Euro", "EU"),
            "BTC" to CurrencyFreaksDetailDto("BTC", "Bitcoin", "Crypto")
        ))
        val rates = CurrencyFreaksRatesDto("USD", "today", mapOf("EUR" to "0.8"))

        val currencies = supported.toFiatCurrencyItems(rates)

        assertEquals(2, currencies.size)
        assertEquals(1.0, currencies.first { it.id == "USD" }.priceInUsd, 0.0)
        assertEquals(1.25, currencies.first { it.id == "EUR" }.priceInUsd, 0.0)
        assertFalse(currencies.any { it.id == "BTC" })
    }

    @Test
    fun realmMapperKeepsCurrencyFields() {
        val currency = CurrencyItem("VND", "VND", "Vietnam Dong", false,
            symbolChar = "₫", priceInUsd = 0.00004)
        val realm = currency.toRealm()
        val restored = realm.toCurrencyItem()

        assertEquals(currency, restored)
        assertEquals("VND", realm.id)
    }
}
