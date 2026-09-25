package com.example.currency.data.local.asset

import android.content.Context
import com.example.currency.data.mapper.toCoinMarketItem
import com.example.currency.data.remote.dto.CoinMarketDto
import com.example.currency.data.remote.dto.CurrencyFreaksRatesDto
import com.example.currency.data.remote.dto.CurrencyFreaksResponseDto
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.CurrencyItem
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AssetSeedCurrencyData(
    val coinMarkets: List<CoinMarketItem>,
    val fiats: List<CurrencyItem>,
    val cryptoUpdatedAt: Long,
    val fiatUpdatedAt: Long
)

/**
 * Lấy data từ trong folder assets và lưu vào trong cơ sở dữ liệu Realm.
 */
@Singleton
class AssetCurrencySeedDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend fun load(): AssetSeedCurrencyData? = withContext(Dispatchers.IO) {
        runCatching {
            val coinDtos = readJson<Array<CoinMarketDto>>(CRYPTO_FILE)
            val supportedCurrencies = readJson<CurrencyFreaksResponseDto>(FIAT_FILE)
            val rates = readJson<CurrencyFreaksRatesDto>(FIAT_RATES_FILE)

            check(rates.base.equals("USD", ignoreCase = true)) { "Seed rates must use USD as base" }
            val coinMarkets = coinDtos.mapNotNull { dto ->
                val assetPath = assetPathOrNull(COINS_DIRECTORY, dto.symbol) ?: return@mapNotNull null
                dto.toCoinMarketItem().let { market ->
                    market.copy(currency = market.currency.copy(iconUrl = assetPath))
                }
            }
            val fiats = supportedCurrencies.supportedCurrenciesMap.values.mapNotNull { detail ->
                val code = detail.id ?: return@mapNotNull null
                val name = detail.name ?: return@mapNotNull null
                if (!detail.status.equals(AVAILABLE_STATUS, ignoreCase = true) || detail.countryCode == CRYPTO_COUNTRY_CODE) {
                    return@mapNotNull null
                }
                val assetPath = assetPathOrNull(FIATS_DIRECTORY, code) ?: return@mapNotNull null
                val rate = rates.rates[code]?.toDoubleOrNull()
                val priceInUsd = when {
                    code.equals("USD", ignoreCase = true) -> 1.0
                    rate != null && rate != 0.0 -> 1.0 / rate
                    else -> return@mapNotNull null
                }
                CurrencyItem(
                    id = code,
                    symbol = code,
                    name = name,
                    isCrypto = false,
                    iconUrl = assetPath,
                    priceInUsd = priceInUsd
                )
            }

            val cryptoUpdatedAt = coinDtos.mapNotNull { it.lastUpdated?.let(::parseCryptoTimestamp) }.minOrNull()
            val fiatUpdatedAt = parseFiatTimestamp(rates.date)
            check(coinMarkets.isNotEmpty() && fiats.isNotEmpty()) { "Seed currencies were empty" }
            checkNotNull(cryptoUpdatedAt) { "Seed crypto timestamp was missing" }
            checkNotNull(fiatUpdatedAt) { "Seed fiat timestamp was invalid" }

            AssetSeedCurrencyData(coinMarkets, fiats, cryptoUpdatedAt, fiatUpdatedAt)
        }.getOrNull()
    }

    /**
     * Hàm này dùng để đọc 1 file Json trong thư mục assets rồi chuyển đổi nó thành 1 đối tượng kiểu T
     */
    private inline fun <reified T> readJson(fileName: String): T =
        context.assets.open(fileName).bufferedReader().use { reader ->
            gson.fromJson(reader, T::class.java)
        }

    /**
     * Hàm này dùng để lấy đường dẫn của 1 file ảnh trong thư mục assets,cách hoạt động:
     */
    private fun assetPathOrNull(directory: String, code: String): String? {
        val fileName = "${code.lowercase(Locale.ROOT)}.png"
        val assetName = "$directory/$fileName"
        return runCatching {
            context.assets.open(assetName).use { input ->
                if (input.read() == -1) null else "file:///android_asset/$assetName"
            }
        }.getOrNull()
    }

    private fun parseCryptoTimestamp(value: String): Long? =
        parseTimestamp(value, "yyyy-MM-dd'T'HH:mm:ss.SSSX")

    private fun parseFiatTimestamp(value: String): Long? =
        parseTimestamp(value, "yyyy-MM-dd HH:mm:ssX")

    private fun parseTimestamp(value: String, pattern: String): Long? = runCatching {
        SimpleDateFormat(pattern, Locale.US).parse(value)?.time
    }.getOrNull()

    private companion object {
        const val CRYPTO_FILE = "currency_seed.json"
        const val FIAT_FILE = "fiat_seed.json"
        const val FIAT_RATES_FILE = "fiat_rates_seed.json"
        const val COINS_DIRECTORY = "coins"
        const val FIATS_DIRECTORY = "fiats"
        const val AVAILABLE_STATUS = "AVAILABLE"
        const val CRYPTO_COUNTRY_CODE = "Crypto"
    }
}
