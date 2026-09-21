package com.example.currency.domain.usecase.currency

import com.example.currency.domain.model.RefreshResult
import com.example.currency.domain.repository.CurrencyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class RefreshCurrenciesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(): RefreshResult = coroutineScope {
        val crypto = async { repository.refreshCoinMarket() }
        val fiat = async { repository.refreshFiats() }
        RefreshResult(crypto.await(), fiat.await())
    }
}
