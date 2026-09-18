package com.example.currency.data.remote.api

import com.example.currency.data.remote.dto.CurrencyFreaksRatesDto
import com.example.currency.data.remote.dto.CurrencyFreaksResponseDto
import retrofit2.http.GET

interface CurrencyFreaksApi {
    @GET("v2.0/rates/latest")
    suspend fun getLatestRates(): CurrencyFreaksRatesDto

    @GET("v2.0/supported-currencies")
    suspend fun getSupportedCurrencies(): CurrencyFreaksResponseDto

    @GET("v2.0/currency-symbols")
    suspend fun getCurrencySymbols(): Map<String, String>
}
