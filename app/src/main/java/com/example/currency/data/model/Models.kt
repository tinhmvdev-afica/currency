package com.example.currency.data.model

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale


data class CoinMarket(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("market_cap") val marketCap: Double? = null,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null
)

data class CurrencyFreaksDetail(
    @SerializedName("currencyCode") val id: String?,
    @SerializedName("currencyName") val name: String?,
    @SerializedName("countryCode") val countryCode: String?,
    val icon: String? = null
)


data class CurrencyFreaksResponse(
    val supportedCurrenciesMap: Map<String, CurrencyFreaksDetail>
)
data class RatesResponse(
    val base: String,
    val date: String,
    val rates: Map<String, String>
)

data class CurrencyRate(
    val currencyCode: String,
    val rate: Double
)

/**
 * Model phản hồi từ endpoint /exchange_rates của CoinGecko
 */
data class ExchangeRatesResponse(
    @SerializedName("rates") val rates: Map<String, ExchangeRateItem>
)

data class ExchangeRateItem(
    @SerializedName("name") val name: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("value") val value: Double,
    @SerializedName("type") val type: String // "fiat" hoặc "crypto"
)

data class CurrencyFreaksRatesResponse(
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, String>
)

/**
 * Model thống nhất cho cả Crypto và Fiat dùng trên giao diện người dùng (UI)
 */
data class CurrencyItem(
    val id: String,
    val symbol: String,
    val name: String,
    val isCrypto: Boolean,
    val iconUrl: String? = null,
    val symbolChar: String = "",
    val priceInUsd: Double = 0.0,
    val priceChange24h: Double? = null
)



fun CoinMarket.toCurrencyItem(usdPrice: Double? = null): CurrencyItem {
    return CurrencyItem(
        id = this.id,
        symbol = this.symbol.uppercase(),
        name = this.name,
        isCrypto = true,
        iconUrl = this.image,
        symbolChar = when (this.symbol.lowercase()) {
            "btc" -> "₿"
            "eth" -> "Ξ"
            "usdt" -> "₮"
            "doge" -> "Ð"
            else -> this.symbol.take(3).uppercase()
        },
        priceInUsd = usdPrice ?: this.currentPrice,
        priceChange24h = this.priceChangePercentage24h
    )
}

/**
 * Định dạng số tiền hiển thị chuẩn theo ngôn ngữ và đơn vị tiền tệ
 */
fun formatAmountDisplay(value: Double, symbol: String = ""): String {
    val isZeroDecimal = symbol.uppercase() in listOf("VND", "JPY", "KRW")
    return if (isZeroDecimal || value >= 1000.0) {
        val longVal = value.toLong()
        if (isZeroDecimal || Math.abs(value - longVal) < 0.001) {
            NumberFormat.getNumberInstance(Locale.US).format(longVal)
        } else {
            NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }.format(value)
        }
    } else if (value >= 1.0) {
        String.format(Locale.US, "%,.2f", value)
    } else if (value >= 0.0001) {
        String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
    } else if (value > 0.0) {
        String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
    } else {
        "0"
    }
}

/**
 * Model hiển thị danh sách quy đổi nhanh đa tiền tệ
 */
data class QuickConversionItem(
    val currency: CurrencyItem,
    val convertedAmount: Double,
    val formattedAmount: String
)
