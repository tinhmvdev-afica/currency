package com.example.currency.domain.usecase.currency

import com.example.currency.domain.model.RefreshResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class RefreshCurrenciesUseCase @Inject constructor(
    private val getCryptoList: GetCryptoListUseCase,
    private val getFiatList: GetFiatListUseCase
) {
    suspend operator fun invoke(): RefreshResult = coroutineScope {
        val crypto = async { getCryptoList(forceRefresh = true) }
        val fiat = async { getFiatList(forceRefresh = true) }
        RefreshResult(crypto.await(), fiat.await())
    }
}
