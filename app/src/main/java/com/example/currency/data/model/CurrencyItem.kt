package com.example.currency.data.model

data class QuickCurrencyItem(
    val symbol: String,
    val name: String,
    val iconText: String,
    val convertedAmount: String,
    val subText: String,
    val isChangeBadge: Boolean = false,
    val isPositive: Boolean = false
)

data class OnboardingSlideItem(
    val icon: String,
    val title: String,
    val description: String
)
