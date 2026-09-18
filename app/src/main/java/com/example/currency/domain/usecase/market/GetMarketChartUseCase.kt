package com.example.currency.domain.usecase.market

import com.example.currency.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetMarketChartUseCase @Inject constructor(private val repository: CurrencyRepository) {
    operator fun invoke(id: String, days: Int = 1) = repository.getMarketChart(id, days)
}
