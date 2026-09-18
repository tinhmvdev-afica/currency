package com.example.currency.di

import android.util.Log
import com.example.currency.BuildConfig
import com.example.currency.data.remote.api.CoinGeckoApi
import com.example.currency.data.remote.api.CurrencyFreaksApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinGeckoRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyFreaksRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val LOG_TAG = "CurrencyApi"
    private val requestSequence = AtomicLong(0)

    private fun Request.redactedUrl(): String = url.newBuilder().apply {
        url.queryParameterNames
            .filter { it.equals("apikey", true) || it.equals("api_key", true) }
            .forEach { setQueryParameter(it, "••••") }
    }.build().toString()

    @Provides
    @Singleton
    fun provideBaseClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val id = requestSequence.incrementAndGet()
            val startedAt = System.nanoTime()
            val safeUrl = request.redactedUrl()
            if (BuildConfig.DEBUG) Log.d(LOG_TAG, "#$id --> ${request.method} $safeUrl")
            try {
                val response = chain.proceed(request)
                if (BuildConfig.DEBUG) {
                    val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                    val size = response.body?.contentLength()?.takeIf { it >= 0 }?.let { "$it B" } ?: "unknown size"
                    Log.d(LOG_TAG, "#$id <-- ${response.code} ${response.message} (${elapsedMs}ms, $size) $safeUrl")
                }
                response
            } catch (error: Exception) {
                if (BuildConfig.DEBUG) {
                    val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                    Log.e(LOG_TAG, "#$id <-- FAILED (${elapsedMs}ms) $safeUrl", error)
                }
                throw error
            }
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @CoinGeckoRetrofit
    fun provideCoinGeckoRetrofit(baseClient: OkHttpClient): Retrofit {
        val client = baseClient.newBuilder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("x-cg-demo-api-key", BuildConfig.COINGECKO_API_KEY)
                .build()
            if (BuildConfig.DEBUG) {
                Log.d(
                    LOG_TAG,
                    "CoinGecko API key attached=${!request.header("x-cg-demo-api-key").isNullOrBlank()} " +
                        "to ${request.url.encodedPath}"
                )
            }
            chain.proceed(request)
        }.build()
        return retrofit("https://api.coingecko.com/api/v3/", client)
    }

    @Provides
    @Singleton
    @CurrencyFreaksRetrofit
    fun provideCurrencyFreaksRetrofit(baseClient: OkHttpClient): Retrofit {
        val client = baseClient.newBuilder().addInterceptor { chain ->
            val request = chain.request()
            val authenticated = request.newBuilder().url(
                request.url.newBuilder()
                    .setQueryParameter("apikey", BuildConfig.CURRENCYFREAKS_API_KEY)
                    .build()
            ).build()
            if (BuildConfig.DEBUG) {
                Log.d(
                    LOG_TAG,
                    "CurrencyFreaks API key attached=${!authenticated.url.queryParameter("apikey").isNullOrBlank()} " +
                        "to ${authenticated.url.encodedPath}"
                )
            }
            chain.proceed(authenticated)
        }.build()
        return retrofit("https://api.currencyfreaks.com/", client)
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideCoinGeckoApi(@CoinGeckoRetrofit retrofit: Retrofit): CoinGeckoApi =
        retrofit.create(CoinGeckoApi::class.java)

    @Provides
    @Singleton
    fun provideCurrencyFreaksApi(@CurrencyFreaksRetrofit retrofit: Retrofit): CurrencyFreaksApi =
        retrofit.create(CurrencyFreaksApi::class.java)
}
