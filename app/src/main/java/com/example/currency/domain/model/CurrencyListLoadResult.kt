package com.example.currency.domain.model

data class CurrencyListLoadResult<out T>(
    val items: List<T>,
    val updatedAt: Long? = null,
    val isRefreshing: Boolean = false
)
