package com.example.currency.data.repository

import android.util.Log
import com.example.currency.data.api.CoinGeckoApi
import com.example.currency.data.api.CurrencyFreaksApi
import com.example.currency.data.local.CurrencyRealm
import com.example.currency.data.local.RealmDatabase.realm
import com.example.currency.data.model.CurrencyItem
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import com.example.currency.data.local.RefreshInfo
import com.example.currency.data.model.PricePoint
import kotlin.text.get

import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class CurrencyRepository @Inject constructor(
    private val coinGeckoApi: CoinGeckoApi,
    private val currencyFreaksApi: CurrencyFreaksApi
) {
    companion object {

        private const val CRYPTO_KEY = "crypto"
        private const val FIAT_KEY = "fiat"

        private const val CRYPTO_REFRESH_TIME =
            5 * 60 * 1000L // 5 phút

        private const val FIAT_REFRESH_TIME =
            30 * 60 * 1000L // 30 phút
    }

    suspend fun getCoinMarket(currency: String= "usd",forceRefresh: Boolean = false): Result<List<CurrencyItem>>{
        return try {
            val localData = getCryptosFromLocal()
            if (localData.isNotEmpty() && !shouldRefresh(CRYPTO_KEY, CRYPTO_REFRESH_TIME)) {
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
            updateRefreshTime(CRYPTO_KEY)
            Result.success(items)
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }
    //getFiats đang gọi cùng lúc 2 API
    suspend fun getFiats(forceRefresh: Boolean = false): Result<List<CurrencyItem>>{
        return try{
            val localData = getFiatsFromLocal()
            if (localData.isNotEmpty() && !shouldRefresh(FIAT_KEY, FIAT_REFRESH_TIME)) {
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
            saveCurrencies(items)
            updateRefreshTime(FIAT_KEY)
            Result.success(items)

        } catch (e: Exception){
            Result.failure(e)
        }
    }
    private fun shouldRefresh(key: String, refreshTime: Long): Boolean {

        val refreshInfo = realm
            .query<RefreshInfo>(
                "key == $0",
                key
            )
            .first()
            .find()

        // Chưa từng refresh
        if (refreshInfo == null) {
            return true
        }

        val now = System.currentTimeMillis()

        return now - refreshInfo.lastUpdated >= refreshTime
    }
    private suspend fun updateRefreshTime(key: String) {
        realm.write {
            val refreshInfo = query<RefreshInfo>(
                "key == $0",
                key
            )
                .first()
                .find()
            if (refreshInfo == null) {
                copyToRealm(
                    RefreshInfo().apply {
                        this.key = key
                        lastUpdated =
                            System.currentTimeMillis()
                    }
                )
            } else {
                refreshInfo.lastUpdated =
                    System.currentTimeMillis()
            }
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
    suspend fun getMarketChart(id :String,currency :String, days: Int = 1):Result<List<PricePoint>>{
        return try{
            val result = coinGeckoApi.getCoinMarketChart(
                id = id,
                vsCurrency = currency,
                days = days
            )
            val items = result.prices.map { item ->
                PricePoint(
                    timestamp = item[0].toLong(),
                    price = item[1]
                )
            }
            Result.success(items)
        }catch (e: CancellationException) {
            throw e
        } catch (e: Exception){
            Result.failure(e)

        }
    }
}
