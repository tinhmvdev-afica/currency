package com.example.currency.domain.model

sealed interface ChartLoadResult {
    data class Data(
        val points: List<PricePoint>,
        val updatedAt: Long,
        val isCached: Boolean,
        val isOffline: Boolean = false,
        val isRefreshing: Boolean = false,
        val refreshFailed: Boolean = false
    ) : ChartLoadResult

    data object OfflineNoCache : ChartLoadResult

    data object NetworkError : ChartLoadResult
}
