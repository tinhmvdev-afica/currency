package com.example.currency.data.api

import com.example.currency.data.model.CurrencyFreaksRatesResponse
import com.example.currency.data.model.CurrencyFreaksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyFreaksApi {
    @GET("v2.0/rates/latest")
    suspend fun getLatestRates(@Query("apikey") apiKey: String): CurrencyFreaksRatesResponse

    @GET("v2.0/supported-currencies")
    suspend fun getSupportedCurrencies(): CurrencyFreaksResponse

    // Bản rút gọn chỉ gồm Map<Code, Name> (tùy chọn)
    @GET("v2.0/currency-symbols")
    suspend fun getCurrencySymbols(): Map<String, String>
}
