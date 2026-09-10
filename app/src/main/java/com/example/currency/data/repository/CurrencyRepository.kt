package com.example.currency.data.repository

import android.util.Log
import com.example.currency.data.api.CoinGeckoApi
import com.example.currency.data.api.CurrencyFreaksApi
import com.example.currency.data.api.RetrofitClient
import com.example.currency.data.local.CurrencyRealm
import com.example.currency.data.local.RealmDatabase.realm
//import com.example.currency.data.local.CurrencyRealm

import com.example.currency.data.model.CoinMarket
import com.example.currency.data.model.CurrencyFreaksDetail
import com.example.currency.data.model.CurrencyFreaksRatesResponse
import com.example.currency.data.model.CurrencyFreaksResponse
import com.example.currency.data.model.CurrencyItem
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlin.text.get

class CurrencyRepository(
    private val coinGeckoApi: CoinGeckoApi = RetrofitClient.api,
    private val currencyFreaksApi: CurrencyFreaksApi = RetrofitClient.currencyFreaksApi
) {
    suspend fun getCoinMarket(currency: String= "vnd"): Result<List<CurrencyItem>>{
        return try {
            val localData = getCryptosFromLocal()
            if (localData.isNotEmpty()) {
                return Result.success(localData)
            }
            val result = coinGeckoApi.getCoinMarkets(
                currency = currency
            )
            val items = result.map { detail ->

                CurrencyItem(
                    id = detail.id,
                    symbol = detail.symbol.uppercase(),
                    name = detail.name,
                    isCrypto = true,
                    iconUrl = detail.image,
                    symbolChar = detail.symbol.take(3).uppercase(),
                    priceInUsd = detail.currentPrice,
                    priceChange24h = detail.priceChangePercentage24h,
                    marketCap = detail.marketCap,
                    marketCapRank = detail.marketCapRank
                )
            }
            saveCurrencies(items)
            Result.success(items)
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }
    //getFiats đang gọi cùng lúc 2 API
    suspend fun getFiats(): Result<List<CurrencyItem>>{
        return try{
            val localData = getFiatsFromLocal()
            if (localData.isNotEmpty()) {
                return Result.success(localData)
            }
            // 1. Lấy supported currencies
            val supportedResponse =
                currencyFreaksApi.getSupportedCurrencies()
            // 2. Lấy rates
            val ratesResponse =
                currencyFreaksApi.getLatestRates("f8071586edf84813987e90fb76f87f1f")

            // 3. Ghép 2 response bằng currency code
            val items = supportedResponse
                .supportedCurrenciesMap
                .values
                .mapNotNull { detail ->
                    val id = detail.id
                        ?: return@mapNotNull null
                    val name = detail.name
                        ?: return@mapNotNull null
                    // Bỏ crypto
                    if (detail.countryCode == "Crypto") {
                        return@mapNotNull null
                    }
                    val rate = ratesResponse.rates[id]
                        ?.toDoubleOrNull()
                    val priceInUsd = when {
                        id == "USD" -> 1.0
                        rate != null && rate != 0.0 ->
                            1.0 / rate
                        else -> 0.0
                    }
                    CurrencyItem(
                        id = id,
                        symbol = id,
                        name = name,
                        isCrypto = false,
                        iconUrl = detail.icon,
                        priceInUsd = priceInUsd
                    )
                }
            Log.d("CHECK_FIAT", "Tổng số đồng fiat lấy được: ${items.size}")
            items.take(5).forEach { item ->
                Log.d("CHECK_FIAT", "Item: id=${item.id}, symbol=${item.symbol}, name=${item.name}, iconurl = ${item.iconUrl}")
            }
            saveCurrencies(items)

            Result.success(items)

        } catch (e: Exception){
            Result.failure(e)
        }
    }
    suspend fun getAllCurrencies(): Result<List<CurrencyItem>> {
        return try {
            val localData = getCurrenciesFromLocal()

            if (localData.isNotEmpty()) {
                Log.d(
                    "CURRENCY_DATA",
                    "Lấy ${localData.size} currencies từ Realm"
                )

                return Result.success(localData)
            }

            Log.d(
                "CURRENCY_DATA",
                "Realm rỗng, bắt đầu gọi API"
            )

            val cryptoResult = getCoinMarket("usd")
            val fiatResult = getFiats()

            cryptoResult.getOrThrow()
            fiatResult.getOrThrow()

            val result = getCurrenciesFromLocal()

            Result.success(result)

        } catch (e: Exception) {
            Log.e(
                "CURRENCY_DATA",
                "getAllCurrencies error",
                e
            )

            Result.failure(e)
        }
    }

    suspend fun saveCurrencies(currencies: List<CurrencyItem>) {
        realm.write {

            currencies.forEach { currency ->

                copyToRealm(
                    CurrencyRealm().apply {
                        id = currency.id
                        symbol = currency.symbol
                        name = currency.name
                        isCrypto = currency.isCrypto
                        iconUrl = currency.iconUrl
                        symbolChar = currency.symbolChar
                        priceInUsd = currency.priceInUsd
                        priceChange24h = currency.priceChange24h
                        marketCap = currency.marketCap
                        marketCapRank = currency.marketCapRank
                    },
                    updatePolicy = UpdatePolicy.ALL
                )
            }
        }
    }
    fun getCurrenciesFromLocal(): List<CurrencyItem> {
        return realm
            .query<CurrencyRealm>()
            .find()
            .map { item ->
                CurrencyItem(
                    id = item.id,
                    symbol = item.symbol,
                    name = item.name,
                    isCrypto = item.isCrypto,
                    iconUrl = item.iconUrl,
                    symbolChar = item.symbolChar,
                    priceInUsd = item.priceInUsd,
                    priceChange24h = item.priceChange24h
                )
            }
    }
    fun getCryptosFromLocal(): List<CurrencyItem> {
        return realm
            .query<CurrencyRealm>("isCrypto == true")
            .find()
            .map { item ->
                CurrencyItem(
                    id = item.id,
                    symbol = item.symbol,
                    name = item.name,
                    isCrypto = true,
                    iconUrl = item.iconUrl,
                    symbolChar = item.symbolChar,
                    priceInUsd = item.priceInUsd,
                    priceChange24h = item.priceChange24h,
                    marketCap = item.marketCap,
                    marketCapRank = item.marketCapRank
                )
            }
    }
    fun getFiatsFromLocal(): List<CurrencyItem> {
        return realm
            .query<CurrencyRealm>("isCrypto == false")
            .find()
            .map { item ->
                CurrencyItem(
                    id = item.id,
                    symbol = item.symbol,
                    name = item.name,
                    isCrypto = false,
                    iconUrl = item.iconUrl,
                    symbolChar = item.symbolChar,
                    priceInUsd = item.priceInUsd,
                    priceChange24h = item.priceChange24h
                )
            }
    }
}