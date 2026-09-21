package com.example.currency.domain.usecase.currency

import com.example.currency.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetCryptoListUseCase @Inject constructor(private val repository: CurrencyRepository) {
    operator fun invoke(forceRefresh: Boolean = false) = repository.getCoinMarket(forceRefresh)
}
