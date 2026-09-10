package com.example.currency.data.model

data class QuickCurrencyItem(
    val symbol: String,
    val name: String,
    val iconUrl: String,
    val convertedAmount: String,
    val subText: String

)

data class OnboardingSlideItem(
    val icon: String,
    val title: String,
    val description: String
)
