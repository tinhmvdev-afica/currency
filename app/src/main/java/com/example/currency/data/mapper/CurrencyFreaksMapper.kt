package com.example.currency.data.mapper

import com.example.currency.data.remote.dto.CurrencyFreaksRatesDto
import com.example.currency.data.remote.dto.CurrencyFreaksResponseDto
import com.example.currency.domain.model.CurrencyItem

fun CurrencyFreaksResponseDto.toFiatCurrencyItems(rates: CurrencyFreaksRatesDto): List<CurrencyItem> =
    supportedCurrenciesMap.values.mapNotNull { detail ->
        val id = detail.id ?: return@mapNotNull null
        val name = detail.name ?: return@mapNotNull null
        if (detail.countryCode == "Crypto") return@mapNotNull null
        val rate = rates.rates[id]?.toDoubleOrNull()
        val priceInUsd = when {
            id == "USD" -> 1.0
            rate != null && rate != 0.0 -> 1.0 / rate
            else -> 0.0
        }
        CurrencyItem(id = id, symbol = id, name = name, isCrypto = false,
            iconUrl = detail.icon, priceInUsd = priceInUsd)
    }

