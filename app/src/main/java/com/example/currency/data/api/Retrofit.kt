package com.example.currency.data.api

import android.util.Log
import com.example.currency.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object RetrofitClient {
    private const val LOG_TAG = "CurrencyApi"
    private const val BASE_URL = "https://api.coingecko.com/api/v3/"
    private const val CURRENCY_FREAKS_BASE_URL = "https://api.currencyfreaks.com/"
    private val requestSequence = AtomicLong(0)

    private fun Request.redactedUrl(): String =
        url.newBuilder().apply {
            url.queryParameterNames
                .filter { it.equals("apikey", ignoreCase = true) || it.equals("api_key", ignoreCase = true) }
                .forEach { setQueryParameter(it, "••••") }
        }.build().toString()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val requestId = requestSequence.incrementAndGet()
                val startedAt = System.nanoTime()
                val safeUrl = request.redactedUrl()

                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "#$requestId --> ${request.method} $safeUrl")
                }

                try {
                    val response = chain.proceed(request)
                    val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                    if (BuildConfig.DEBUG) {
                        val size = response.body?.contentLength()?.takeIf { it >= 0 }?.let { "$it B" } ?: "unknown size"
                        Log.d(
                            LOG_TAG,
                            "#$requestId <-- ${response.code} ${response.message} (${elapsedMs}ms, $size) $safeUrl"
                        )
                    }
                    response
                } catch (error: Exception) {
                    val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "#$requestId <-- FAILED (${elapsedMs}ms) $safeUrl", error)
                    }
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
