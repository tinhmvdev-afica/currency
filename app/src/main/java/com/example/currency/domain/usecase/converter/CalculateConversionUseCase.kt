package com.example.currency.domain.usecase.converter

import com.example.currency.domain.model.CurrencyItem
import javax.inject.Inject

class CalculateConversionUseCase @Inject constructor() {
    operator fun invoke(amount: Double, from: CurrencyItem, to: CurrencyItem): Double =
        if (to.priceInUsd == 0.0) 0.0 else amount * from.priceInUsd / to.priceInUsd
}
