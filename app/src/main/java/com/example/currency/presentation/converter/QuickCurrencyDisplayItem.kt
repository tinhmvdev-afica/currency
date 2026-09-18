package com.example.currency.presentation.converter

data class QuickCurrencyDisplayItem(
    val symbol: String,
    val name: String,
    val iconUrl: String,
    val convertedAmount: String,
    val subText: String
)
