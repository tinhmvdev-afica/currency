package com.example.currency.data.repository

import android.util.Log
import com.example.currency.data.api.CoinGeckoApi
import com.example.currency.data.api.CurrencyFreaksApi
import com.example.currency.data.api.RetrofitClient
//import com.example.currency.data.local.CurrencyRealm

import com.example.currency.data.model.CoinMarket
import com.example.currency.data.model.CurrencyFreaksDetail
import com.example.currency.data.model.CurrencyFreaksRatesResponse
import com.example.currency.data.model.CurrencyFreaksResponse
import com.example.currency.data.model.CurrencyItem
//import com.example.currency.data.local.RealmDatabase
//import io.realm.kotlin.UpdatePolicy
//import io.realm.kotlin.ext.query
import kotlin.text.get

class CurrencyRepository(
    private val coinGeckoApi: CoinGeckoApi = RetrofitClient.api,
    private val currencyFreaksApi: CurrencyFreaksApi = RetrofitClient.currencyFreaksApi
) {
//    private val realm = RealmDatabase.realm
    suspend fun getCoinMarket(currency: String= "vnd"): Result<List<CurrencyItem>>{
        return try {
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
                    symbolChar = when (detail.symbol.lowercase()) {
                        "btc" -> "₿"
                        "eth" -> "Ξ"
                        "usdt" -> "₮"
                        "doge" -> "Ð"
                        else -> detail.symbol.take(3).uppercase()
                    },
                    priceInUsd = detail.currentPrice,
                    priceChange24h = detail.priceChangePercentage24h
                )
            }
            Result.success(items)
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }
    suspend fun getFiats(): Result<List<CurrencyItem>>{
        return try{
            val result = currencyFreaksApi.getSupportedCurrencies()
            val items = result.supportedCurrenciesMap.values.mapNotNull { detail ->
                val id = detail.id ?: return@mapNotNull null
                val name = detail.name ?: return@mapNotNull null
                if (detail.countryCode == "Crypto") return@mapNotNull null
                CurrencyItem(
                    id = detail.id,
                    name = detail.name,
                    symbol = detail.id,
                    isCrypto = false,
                    iconUrl = detail.icon
                )
            }
            Log.d("CHECK_FIAT", "Tổng số đồng fiat lấy được: ${items.size}")
            items.take(5).forEach { item ->
                Log.d("CHECK_FIAT", "Item: id=${item.id}, symbol=${item.symbol}, name=${item.name}, iconurl = ${item.iconUrl}")
            }
            Result.success(items)

        } catch (e: Exception){
            Result.failure(e)
        }
    }

//    suspend fun saveCurrencies(
//        currencies: List<CurrencyItem>
//    ) {
//        realm.write {
//
//            currencies.forEach { currency ->
//
//                copyToRealm(
//                    CurrencyRealm().apply {
//                        id = currency.id
//                        symbol = currency.symbol
//                        name = currency.name
//                        isCrypto = currency.isCrypto
//                        iconUrl = currency.iconUrl
//                        symbolChar = currency.symbolChar
//                        priceInUsd = currency.priceInUsd
//                        priceChange24h = currency.priceChange24h
//                    },
//                    updatePolicy = UpdatePolicy.ALL
//                )
//            }
//        }
//    }
//    fun getCurrencies(): List<CurrencyItem> {
//
//        return realm
//            .query<CurrencyRealm>()
//            .find()
//            .map { item ->
//
//                CurrencyItem(
//                    id = item.id,
//                    symbol = item.symbol,
//                    name = item.name,
//                    isCrypto = item.isCrypto,
//                    iconUrl = item.iconUrl,
//                    symbolChar = item.symbolChar,
//                    priceInUsd = item.priceInUsd,
//                    priceChange24h = item.priceChange24h
//                )
//            }
//    }
}