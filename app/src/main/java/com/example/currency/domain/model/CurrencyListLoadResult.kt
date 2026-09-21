package com.example.currency.domain.model

sealed interface CurrencyListLoadResult<out T> {
    data class Data<T>(
        val items: List<T>,
        val updatedAt: Long? = null,
        val isCached: Boolean,
        val isOffline: Boolean = false,
        val isRefreshing: Boolean = false,
        val refreshFailed: Boolean = false
    ) : CurrencyListLoadResult<T>

    data object OfflineNoCache : CurrencyListLoadResult<Nothing>
    data object NetworkError : CurrencyListLoadResult<Nothing>
}
