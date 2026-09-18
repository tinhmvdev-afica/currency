package com.example.currency.presentation.onboarding

data class OnboardingSlideItem(
    val icon: String,
    val title: String,
    val description: String
)

object OnboardingSlides {
    val list: List<OnboardingSlideItem> = listOf(
        OnboardingSlideItem(
            icon = "⚡",
            title = "Instant Multi-Currency Converter",
            description = "Convert seamlessly between fiat currencies (USD, VND, EUR) and over 100 leading cryptocurrencies."
        ),
        OnboardingSlideItem(
            icon = "📈",
            title = "CoinGecko-Powered Data",
            description = "Real-time market rates with accurate data from the CoinGecko API."
        ),
        OnboardingSlideItem(
            icon = "🎯",
            title = "Easy Tracking & Customization",
            description = "Track favorite coins, view 24-hour changes, and set a default base currency."
        )
    )
}
