package com.example.currency

import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.domain.model.ChartLoadResult
import com.example.currency.domain.repository.CurrencyRepository
import com.example.currency.domain.usecase.converter.CalculateConversionUseCase
import com.example.currency.domain.usecase.currency.GetCryptoListUseCase
import com.example.currency.domain.usecase.currency.GetFiatListUseCase
import com.example.currency.domain.usecase.currency.RefreshCurrenciesUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversionAndRefreshUseCaseTest {
    private val bitcoin = CurrencyItem("bitcoin", "BTC", "Bitcoin", true, priceInUsd = 100.0)
    private val usd = CurrencyItem("usd", "USD", "US Dollar", false, priceInUsd = 1.0)
    private val zero = CurrencyItem("zero", "ZERO", "Zero", false, priceInUsd = 0.0)

    @Test
    fun conversionUsesUsdPricesAndHandlesZeroTarget() {
        val conversion = CalculateConversionUseCase()
        assertEquals(200.0, conversion(2.0, bitcoin, usd), 0.0)
        assertEquals(0.02, conversion(2.0, usd, bitcoin), 0.0)
        assertEquals(0.0, conversion(2.0, bitcoin, zero), 0.0)
    }

    @Test
    fun refreshRequestsBothSourcesAndKeepsSuccessfulResult() = runBlocking {
        val repository = object : CurrencyRepository {
            var cryptoForced = false
            var fiatForced = false

            override suspend fun getCoinMarket(forceRefresh: Boolean): Result<List<CoinMarketItem>> {
                cryptoForced = forceRefresh
                return Result.success(listOf(CoinMarketItem(bitcoin, 100.0)))
            }

            override suspend fun getFiats(forceRefresh: Boolean): Result<List<CurrencyItem>> {
                fiatForced = forceRefresh
                return Result.failure(IllegalStateException("fiat unavailable"))
            }

            override fun getMarketChart(id: String, days: Int): Flow<ChartLoadResult> =
                error("Not used")
        }

        val result = RefreshCurrenciesUseCase(GetCryptoListUseCase(repository), GetFiatListUseCase(repository))()

        assertTrue(repository.cryptoForced)
        assertTrue(repository.fiatForced)
        assertEquals(bitcoin, result.crypto.getOrThrow().single().currency)
        assertTrue(result.fiat.isFailure)
    }
}
