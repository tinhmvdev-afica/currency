package com.example.currency.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val LOG_TAG = "CurrencyApi"
    private const val BASE_URL = "https://api.coingecko.com/api/v3/"
    private const val CURRENCY_FREAKS_BASE_URL = "https://api.currencyfreaks.com/"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val startedAt = System.nanoTime()
                Log.d(LOG_TAG, "--> ${request.method} ${request.url}")
                try {
                    val response = chain.proceed(request)
                    val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                    Log.d(LOG_TAG, "<-- ${response.code} ${response.message} (${elapsedMs}ms) ${request.url}")
                    response
                } catch (error: Exception) {
                    val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                    Log.e(LOG_TAG, "<-- FAILED (${elapsedMs}ms) ${request.url}", error)
                    throw error
                }
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }

    val currencyFreaksApi: CurrencyFreaksApi by lazy {
        Retrofit.Builder()
            .baseUrl(CURRENCY_FREAKS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyFreaksApi::class.java)
    }
}
