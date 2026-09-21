package com.example.currency.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CurrencyFreaksDetailDto(
    @SerializedName("currencyCode") val id: String?,
    @SerializedName("currencyName") val name: String?,
    @SerializedName("countryCode") val countryCode: String?,
    val status: String? = null,
    val icon: String? = null
)

data class CurrencyFreaksResponseDto(
    val supportedCurrenciesMap: Map<String, CurrencyFreaksDetailDto>
)

data class CurrencyFreaksRatesDto(
    val base: String,
    val date: String,
    val rates: Map<String, String>
)
