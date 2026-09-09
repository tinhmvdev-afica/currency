package com.example.currency.data.repository

import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.model.OnboardingSlideItem
import com.example.currency.data.model.QuickCurrencyItem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyMockRepository {

    val cryptoList: List<CurrencyItem> = listOf(
        CurrencyItem(
            id = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/1/small/bitcoin.png",
            symbolChar = "₿",
            priceInUsd = 102400.0,
            priceChange24h = -1.85
        ),
        CurrencyItem(
            id = "ethereum",
            symbol = "ETH",
            name = "Ethereum",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/279/small/ethereum.png",
            symbolChar = "Ξ",
            priceInUsd = 3315.20,
            priceChange24h = -2.40
        ),
        CurrencyItem(
            id = "solana",
            symbol = "SOL",
            name = "Solana",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/4128/small/solana.png",
            symbolChar = "◎",
            priceInUsd = 218.40,
            priceChange24h = 6.18
        ),
        CurrencyItem(
            id = "binancecoin",
            symbol = "BNB",
            name = "BNB",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/825/small/bnb-icon2_2x.png",
            symbolChar = "BNB",
            priceInUsd = 645.10,
            priceChange24h = 1.12
        ),
        CurrencyItem(
            id = "tether",
            symbol = "USDT",
            name = "Tether USD",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/325/small/Tether.png",
            symbolChar = "₮",
            priceInUsd = 1.00,
            priceChange24h = 0.02
        ),
        CurrencyItem(
            id = "ripple",
            symbol = "XRP",
            name = "Ripple",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/44/small/xrp-symbol-white-128.png",
            symbolChar = "✕",
            priceInUsd = 2.45,
            priceChange24h = -3.15
        ),
        CurrencyItem(
            id = "dogecoin",
            symbol = "DOGE",
            name = "Dogecoin",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/5/small/dogecoin.png",
            symbolChar = "Ð",
            priceInUsd = 0.26,
            priceChange24h = -4.80
        ),
        CurrencyItem(
            id = "cardano",
            symbol = "ADA",
            name = "Cardano",
            isCrypto = true,
            iconUrl = "https://assets.coingecko.com/coins/images/975/small/cardano.png",
            symbolChar = "₳",
            priceInUsd = 0.82,
            priceChange24h = -1.25
        ),
        CurrencyItem(
            id = "avalanche-2",
            symbol = "AVAX",
            name = "Avalanche",
            isCrypto = true,
            iconUrl = null,
            symbolChar = "▲",
            priceInUsd = 28.50,
            priceChange24h = 3.20
        ),
        CurrencyItem(
            id = "chainlink",
            symbol = "LINK",
            name = "Chainlink",
            isCrypto = true,
            iconUrl = null,
            symbolChar = "⬡",
            priceInUsd = 18.90,
            priceChange24h = 0.80
        ),
        CurrencyItem(
            id = "sui",
            symbol = "SUI",
            name = "Sui Network",
            isCrypto = true,
            iconUrl = null,
            symbolChar = "💧",
            priceInUsd = 3.25,
            priceChange24h = 4.50
        ),
        CurrencyItem(
            id = "shiba-inu",
            symbol = "SHIB",
            name = "Shiba Inu",
            isCrypto = true,
            iconUrl = null,
            symbolChar = "🐕",
            priceInUsd = 0.0000185,
            priceChange24h = -2.10
        ),
        CurrencyItem(
            id = "pepe",
            symbol = "PEPE",
            name = "Pepe",
            isCrypto = true,
            iconUrl = null,
            symbolChar = "🐸",
            priceInUsd = 0.0000112,
            priceChange24h = 8.40
        ),
        CurrencyItem(
            id = "near",
            symbol = "NEAR",
            name = "NEAR Protocol",
            isCrypto = true,
            iconUrl = null,
            symbolChar = "Ⓝ",
            priceInUsd = 5.60,
            priceChange24h = 1.80
        )
    )

    val fiatList: List<CurrencyItem> = listOf(
        CurrencyItem(
            id = "VND",
            symbol = "VND",
            name = "Việt Nam Đồng",
            isCrypto = false,
            symbolChar = "★",
            priceInUsd = 1.0 / 25450.0,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "USD",
            symbol = "USD",
            name = "Đô la Mỹ",
            isCrypto = false,
            symbolChar = "$",
            priceInUsd = 1.0,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "EUR",
            symbol = "EUR",
            name = "Euro Châu Âu",
            isCrypto = false,
            symbolChar = "€",
            priceInUsd = 1.0 / 0.92,
            priceChange24h = -0.35
        ),
        CurrencyItem(
            id = "JPY",
            symbol = "JPY",
            name = "Yên Nhật",
            isCrypto = false,
            symbolChar = "¥",
            priceInUsd = 1.0 / 154.5,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "GBP",
            symbol = "GBP",
            name = "Bảng Anh",
            isCrypto = false,
            symbolChar = "£",
            priceInUsd = 1.0 / 0.79,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "KRW",
            symbol = "KRW",
            name = "Won Hàn Quốc",
            isCrypto = false,
            symbolChar = "₩",
            priceInUsd = 1.0 / 1420.0,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "SGD",
            symbol = "SGD",
            name = "Đô la Singapore",
            isCrypto = false,
            symbolChar = "S$",
            priceInUsd = 1.0 / 1.34,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "AUD",
            symbol = "AUD",
            name = "Đô la Úc",
            isCrypto = false,
            symbolChar = "A$",
            priceInUsd = 1.0 / 1.55,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "CAD",
            symbol = "CAD",
            name = "Đô la Canada",
            isCrypto = false,
            symbolChar = "C$",
            priceInUsd = 1.0 / 1.41,
            priceChange24h = null
        ),
        CurrencyItem(
            id = "CNY",
            symbol = "CNY",
            name = "Nhân dân tệ",
            isCrypto = false,
            symbolChar = "¥",
            priceInUsd = 1.0 / 7.24,
            priceChange24h = null
        )
    )

    val allCurrencies: List<CurrencyItem> = cryptoList + fiatList

    val onboardingSlides: List<OnboardingSlideItem> = listOf(
        OnboardingSlideItem(
            icon = "⚡",
            title = "Quy Đổi Đa Năng Tức Thì",
            description = "Chuyển đổi liền mạch giữa các đồng tiền pháp định (USD, VND, EUR) và hơn 100+ đồng Crypto hàng đầu."
        ),
        OnboardingSlideItem(
            icon = "📈",
            title = "Dữ Liệu Chuẩn CoinGecko",
            description = "Tỷ giá thị trường cập nhật theo thời gian thực với độ chính xác cao từ hệ thống API CoinGecko."
        ),
        OnboardingSlideItem(
            icon = "🎯",
            title = "Theo Dõi & Tùy Biến Dễ Dàng",
            description = "Lập danh sách theo dõi các coin yêu thích, xem biến động 24h và đặt đồng tiền cơ sở mặc định."
        )
    )

    fun calculateRate(from: CurrencyItem, to: CurrencyItem): Double {
        if (to.priceInUsd == 0.0) return 0.0
        return from.priceInUsd / to.priceInUsd
    }

    fun formatNumber(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        return when {
            value >= 1_000_000 -> {
                DecimalFormat("#,###", symbols).format(value)
            }
            value >= 1 -> {
                DecimalFormat("#,##0.00", symbols).format(value)
            }
            value >= 0.0001 -> {
                DecimalFormat("#,##0.0000", symbols).format(value)
            }
            value > 0 -> {
                DecimalFormat("0.00000000", symbols).format(value)
            }
            else -> "0.00"
        }
    }

    fun getQuickConversions(inputAmount: Double, from: CurrencyItem): List<QuickCurrencyItem> {
        val baseUsd = inputAmount * from.priceInUsd

        val usdAmount = baseUsd
        val eurAmount = baseUsd * 0.92
        val ethAmount = if (3315.20 > 0) baseUsd / 3315.20 else 0.0
        val solAmount = if (218.40 > 0) baseUsd / 218.40 else 0.0

        return listOf(
            QuickCurrencyItem(
                symbol = "USD",
                name = "Đô la Mỹ",
                iconText = "$",
                convertedAmount = "$" + formatNumber(usdAmount),
                subText = "Tỷ giá gốc (USD)",
                isChangeBadge = false
            ),
            QuickCurrencyItem(
                symbol = "EUR",
                name = "Euro",
                iconText = "€",
                convertedAmount = "€" + formatNumber(eurAmount),
                subText = "Giảm -0.35% (24h)",
                isChangeBadge = true,
                isPositive = false
            ),
            QuickCurrencyItem(
                symbol = "ETH",
                name = "Ethereum",
                iconText = "Ξ",
                convertedAmount = formatNumber(ethAmount) + " ETH",
                subText = "Giảm -2.40% (24h)",
                isChangeBadge = true,
                isPositive = false
            ),
            QuickCurrencyItem(
                symbol = "SOL",
                name = "Solana",
                iconText = "◎",
                convertedAmount = formatNumber(solAmount) + " SOL",
                subText = "Tăng +6.18% (24h)",
                isChangeBadge = true,
                isPositive = true
            )
        )
    }
}
